package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.service;


import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.exception.ConflictException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.exception.LockTimeoutException;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.exception.OperationNotAllowed;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.Config;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.IdentityProvider;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.State;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.Status;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.SsoRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;


@Service
public class SsoServiceImpl {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SsoRepository ssoRepository;
    private final LockManager lockStore;
    private final ThreadGroup group;

    public SsoServiceImpl(@Autowired SsoRepository ssoRepository, @Autowired LockManager lockManager) {
        this.ssoRepository = ssoRepository;
        this.lockStore = lockManager;
        this.group = new ThreadGroup(this.getClass().getSimpleName());
    }

    public Config provisionSso(IdentityProvider identityProvider, String tenantId) throws ConflictException {
        logger.debug("Starting SSO provisioning for tenant: {}", tenantId);
        Config ssoConfig = new Config(UUID.randomUUID().toString(), identityProvider, new Status(State.IN_ACTIVATION));
        try {
            List<Config> configs = ssoRepository.findAllByTenantId(tenantId);
            // filter configs with same identity provider
            Set<Config> configSet = configs.stream().filter(config -> config.getIdentityProvider().equals(identityProvider)).collect(Collectors.toSet());
            if (!configSet.isEmpty()) {
                throw new ConflictException(configs.getFirst().getId());
            }
            Lock lock = lockStore.getLock(tenantId + ssoConfig.getId());
            if (lock.tryLock(10, java.util.concurrent.TimeUnit.SECONDS)) {
                ssoRepository.save(ssoConfig, tenantId, ssoConfig.getId());
                String key = this.key(tenantId, ssoConfig.getId());
                if (!this.isThreadActive(key)) {
                    Thread thread = new Thread(this.group, getSSoStateWorker(tenantId, ssoConfig.getId()), key);
                    thread.start();
                }
                lock.unlock();
                return ssoConfig;
            } else {
                return provisionSso(identityProvider, tenantId);
            }
        } catch (ConflictException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public List<Config> getSsoConfigs(String tenantId) throws FileNotFoundException {
        logger.debug("Getting SSO configurations for tenant: {}", tenantId);
        try {
            return ssoRepository.findAllByTenantId(tenantId);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error while getting SSO configurations", e);
        }
    }

    public Optional<Config> getSsoConfig(String tenantId, String ssoId) {
        logger.debug("Getting SSO configuration for tenant: {} and ssoId: {}", tenantId, ssoId);
        try {
            return ssoRepository.findByTenantIdAndSsoId(tenantId, ssoId);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public Optional<Config> deleteSsoConfig(String tenantId, String ssoId) throws OperationNotAllowed, LockTimeoutException {
        logger.debug("Deleting SSO configuration for tenant: {} and ssoId: {}", tenantId, ssoId);
        try {
            Optional<Config> optionalConfig = getSsoConfig(tenantId, ssoId);
            if (optionalConfig.isEmpty()) {
                return Optional.empty();
            }
            Config config = optionalConfig.get();
            if (config.getStatus().getState() == State.IN_ACTIVATION ||
                    config.getStatus().getState() == State.IN_DELETION) {
                throw new OperationNotAllowed("Operation not allowed : " + config.getStatus());
            }
            String key = this.key(tenantId, ssoId);
            Lock lock = lockStore.getLock(key);
            if (lock.tryLock(10, java.util.concurrent.TimeUnit.SECONDS)) {
                config.getStatus().setState(State.IN_DELETION);
                ssoRepository.save(config, tenantId, ssoId);
                if (!this.isThreadActive(key)) {
                    Thread thread = new Thread(this.group, getSSoStateWorker(tenantId, ssoId), key);
                    thread.start();
                }
                lock.unlock();
                return Optional.of(config);
            }
            throw new LockTimeoutException("SSO lock timeout");
        } catch (LockTimeoutException | OperationNotAllowed e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting SSO configuration", e);
        }
    }

    private @NotNull SSoStateWorker getSSoStateWorker(String tenantId, String ssoId) {
        return new SSoStateWorker(tenantId, ssoId, ssoRepository, new LockHelper(lockStore, this.key(tenantId, ssoId)));
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

    private String key(String tenantId, String ssoId) {
        return tenantId + "-" + ssoId;
    }

}
