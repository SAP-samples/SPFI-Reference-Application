package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.service;


import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.controller.AppException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.State;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.tenant.Tenant;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.ITenantRepository;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.TenantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.State.*;

public class TenantWorker implements Runnable {

    private final String tenantId;
    private final String fileId;
    private final ITenantRepository repo;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LockHelper lockAssist;
    private final ConcurrentHashMap<String, Tenant> tenantDataMap;

    public TenantWorker(String tenantId, String fileId, ITenantRepository repo, ConcurrentHashMap<String, Tenant> tenantDataMap, LockHelper lock) {
        this.tenantId = tenantId;
        this.fileId = fileId;
        this.repo = repo;
        this.tenantDataMap = tenantDataMap;
        this.lockAssist = lock;
    }

    @Override
    public void run() {
        boolean lockAcquired = false;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                //Thread.sleep(15000);
                TimeUnit.SECONDS.sleep(10);
                // wait and acquire lock
                Lock lock = this.lockAssist.getLock();
                try {
                    if (lock.tryLock(10, TimeUnit.SECONDS)) {
                        lockAcquired = true;
                        Tenant tenant = this.repo.readTenantByTenantId(this.tenantId);
                        if (tenant == null) {
                            logger.info("Tenant not found");
                            return;
                        }
                        handleStateTransition(tenant);

                        if (tenant.getStatus().getState() == State.FINAL_ERROR.getReadableState() ||
                                tenant.getStatus().getState() == State.ACTIVE.getReadableState()) {
                            logger.info("Tenant provisioning {}", tenant.getStatus().getState());
                            return;
                        }
                    }
                } finally {
                    if (lockAcquired) {
                        lock.unlock();
                        lockAcquired = false;
                    }

                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            logger.warn("Tenant provisioning interrupted", e);
        }
    }

    private void handleStateTransition(Tenant tenant) throws AppException {
        TenantUtils tenantUtils = new TenantUtils();
        switch (State.fromReadableState(tenant.getStatus().getState())) {
            case IN_ACTIVATION, IN_SELF_RECOVERABLE_ERROR, IN_RECOVERABLE_ERROR -> {
                logger.info("Tenant - active");
                tenant = tenantUtils.setTenantState(tenant, ACTIVE.getReadableState());
                this.repo.storeTenant(tenant);
            }
            case IN_DELETION -> {
                logger.info("Tenant deletion detected");
                this.repo.deleteTenantByTenantId(tenant.getId());
            }
            case IN_UPDATE -> {
                logger.info("Tenant - update & active");
                Tenant tenantData = tenantDataMap.get(tenant.getId());
                this.repo.updateTenant(tenant.getId(), tenantData);
                tenantDataMap.remove(tenant.getId());
            }
            case IN_BLOCKING -> {
                logger.info("Tenant - block");
                tenant = tenantUtils.setTenantState(tenant, BLOCKED.getReadableState());
                this.repo.storeTenant(tenant);
            }
            default -> {
                logger.warn("Unhandled state: {}", tenant.getStatus().getState());
            }
        }
    }


}
