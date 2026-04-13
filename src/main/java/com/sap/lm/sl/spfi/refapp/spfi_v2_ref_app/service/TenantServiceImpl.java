package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.service;


import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller.AppException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller.NotFoundException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.State;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.*;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.ITenantRepository;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.TenantUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;


@Service
public class TenantServiceImpl implements TenantService {
    private static final String CONFIGURATION_URL_PATTERN_V2 = "{appEndpoint}/v2/tenants/{tenantId}";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ITenantRepository tenantRepository;
    private final LockManager lockStore;
    private final ThreadGroup group;

    @Value("${APPLICATION_ENDPOINT:https://www.sap.com}")
    public String appEndpoint;


    private final ConcurrentHashMap<String, Tenant> tenantDataMap = new ConcurrentHashMap<>(); // A global thread-safe map to store tenant data keyed by tenantId

    public void storeTenantData(String tenantId, Tenant tenantData) { // Method to store tenantData in the global map
        tenantDataMap.put(tenantId, tenantData);
    }

    public TenantServiceImpl(@Autowired LockManager lockManager, @Autowired ITenantRepository tenantRepository) {
        this.lockStore = lockManager;
        this.group =  new ThreadGroup(this.getClass().getSimpleName());
        this.tenantRepository = tenantRepository;
    }

    @Async
    @Override
    public void slowTenantActivation(Tenant tenantData) throws AppException {
        try {
            String tenantId = tenantData.getId();

            Lock lock = lockStore.getLock(tenantId);
            if (lock.tryLock(10, java.util.concurrent.TimeUnit.SECONDS)) {
                fastTenantActivation(tenantData, State.IN_ACTIVATION.getReadableState());
                if (!this.isThreadActive(tenantId)) {
                    Thread thread = new Thread(this.group, getTenantWorker(tenantId, tenantData.getSapId()), tenantId);
                    thread.start();
                }
                lock.unlock();
            } else {
                throw new AppException("Too Many Requests");
            }
        } catch (Exception e) {
            throw new AppException("Error during slow tenant activation", e);
        }
    }


    @Async
    @Override
    public void slowTenantUpdate(Tenant tenant, Tenant tenantData) throws AppException {
            try {
                String tenantId = tenant.getId();
                Lock lock = lockStore.getLock(tenantId);
                if (lock.tryLock(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    fastTenantStatusUpdate(tenantId, new StateRequest(State.IN_UPDATE.getReadableState()));
                    storeTenantData(tenantId, tenantData); // Store tenantData in the global map
                    if (!this.isThreadActive(tenantId)) {
                        Thread thread = new Thread(this.group, getTenantWorker(tenantId, tenant.getSapId()), tenantId);
                        thread.start();
                    }
                    lock.unlock();
                } else {
                    throw new AppException("Too Many Requests");
                }
            } catch (Exception e) {
                throw new AppException("Error during slow tenant update", e);
            }
    }



    @Override
    public Map<String, Object> fastTenantActivation(Tenant tenant, String state) throws AppException {
        logger.debug("Starting tenant activation, CRM-Tenant-ID: {}, State: {}", tenant.getSapId(), state);

        // generating endpoints
        Endpoints endpoints = new Endpoints();
        endpoints.setApplicationUrl(appEndpoint);
        endpoints.setConfigurationUrl(CONFIGURATION_URL_PATTERN_V2.replace("{appEndpoint}", appEndpoint)
                .replace("{tenantId}", tenant.getId()));
        tenant.setEndpoints(endpoints);

        TenantUtils tenantUtils = new TenantUtils();
        tenant = tenantUtils.setTenantState(tenant, state);

        Tenant tenantStored = tenantRepository.storeTenant(tenant);

        // generating response
        Map<String, Object> response = tenantUtils.prepareResponseForProvisioning(tenantStored);

        return response;
    }

    @Override
    public void fastTenantUpdate(String tenantId, Tenant tenantData) throws AppException {
        tenantRepository.updateTenant(tenantId, tenantData);
    }

    @Async
    @Override
    public void slowTenantDeletion(Tenant tenant) throws AppException {
        try {
            String tenantId = tenant.getId();
            Lock lock = lockStore.getLock(tenantId);
            if (lock.tryLock(10, java.util.concurrent.TimeUnit.SECONDS)) {
                fastTenantStatusUpdate(tenantId, new StateRequest(State.IN_DELETION.getReadableState()));
                if (!this.isThreadActive(tenantId)) {
                    Thread thread = new Thread(this.group, getTenantWorker(tenantId, tenant.getSapId()), tenantId);
                    thread.start();
                }
                lock.unlock();
            } else {
                throw new AppException("Too Many Requests");
            }
        } catch (Exception e) {
            throw new AppException("Error during slow tenant deletion", e);
        }
    }

    @Override
    public void fastTenantDeletion(String tenantId) throws AppException {
        logger.debug("Starting tenant deletion, Tenant-ID: {}", tenantId);
        tenantRepository.deleteTenantByTenantId(tenantId);
    }

    @Override
    public Status getTenantStatus(String tenantId) throws AppException {
        logger.debug("Getting status for Tenant, Tenant-ID: {}", tenantId);
        return tenantRepository.getTenantStatus(tenantId);
    }

    @Override
    public Status fastTenantStatusUpdate(String tenantId, StateRequest stateRequest) throws AppException {
        logger.debug("Changing status for Tenant, Tenant-ID: {}, State: {}", tenantId, stateRequest.getState());
        return tenantRepository.changeTenantStatus(tenantId, stateRequest);
    }

    @Async
    @Override
    public void slowTenantStatusUpdate(String tenantId, StateRequest stateRequest, Tenant tenant) throws AppException {
        try {
            Lock lock = lockStore.getLock(tenantId);
            if (lock.tryLock(10, java.util.concurrent.TimeUnit.SECONDS)) {
                Set<String> statesRequiringActivation = Set.of(
                        State.BLOCKED.getReadableState(),
                        State.IN_RECOVERABLE_ERROR.getReadableState()
                );

                Set<String> statesRequiringBlocking = Set.of(
                        State.ACTIVE.getReadableState(),
                        State.IN_RECOVERABLE_ERROR.getReadableState()
                );
                String currentStatus = tenant.getStatus().getState();
                String newStatus = stateRequest.getState();
                if (newStatus.equals(State.ACTIVE.getState()) && statesRequiringActivation.contains(currentStatus)) {
                    fastTenantStatusUpdate(tenantId, new StateRequest(State.IN_ACTIVATION.getReadableState()));
                } else if (newStatus.equals(State.BLOCKED.getState()) && statesRequiringBlocking.contains(currentStatus)) {
                    fastTenantStatusUpdate(tenantId, new StateRequest(State.IN_BLOCKING.getReadableState()));
                }

                if (!this.isThreadActive(tenantId)) {
                    Thread thread = new Thread(this.group, getTenantWorker(tenantId, tenant.getSapId()), tenantId);
                    thread.start();
                }
                lock.unlock();
            } else {
                throw new AppException("Too Many Requests");
            }
        } catch (Exception e) {
            throw new AppException("Error during slow tenant deletion", e);
        }

    }

    @Override
    public Tenant getTenant(String tenantId) throws NotFoundException, AppException {
        return tenantRepository.readTenantByTenantId(tenantId);
    }

    @Override
    public List<Tenant> readTenants() throws AppException {
        return tenantRepository.readTenants();
    }

    private @NotNull TenantWorker getTenantWorker(String tenantId, String sapId) {
        return new TenantWorker(tenantId, tenantId + "_" + sapId, tenantRepository, tenantDataMap, new LockHelper(lockStore, tenantId));
    }

    private Boolean isThreadActive(String name) {
        Thread[] threads = new Thread[this.group.activeCount()];
        this.group.enumerate(threads);
        for (Thread thread : threads) {
            if (thread.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
