package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.service;


import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.Config;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.State;
import com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository.SsoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.model.jpa.sso.State.*;

public class SSoStateWorker implements Runnable {

    private final String TenantId;
    private final String fileId;
    private final SsoRepository repo;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LockHelper lockAssist;

    public SSoStateWorker(String tenantId, String fileId, SsoRepository repo, LockHelper lock) {
        this.TenantId = tenantId;
        this.fileId = fileId;
        this.repo = repo;
        this.lockAssist = lock;
    }

    @Override
    public void run() {
        boolean lockAcquired = false;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // wait and acquire lock
                Thread.sleep(10000);
                Lock lock = this.lockAssist.getLock();
                try {
                    if (lock.tryLock(10, TimeUnit.SECONDS)) {
                        lockAcquired = true;
                        Optional<Config> currentStateOptional = this.repo.findByTenantIdAndSsoId(this.TenantId, this.fileId);
                        if (currentStateOptional.isEmpty()) {
                            logger.info("SSO configuration not found");
                            return;
                        }
                        Config config = currentStateOptional.get();

                        handleStateTransition(config);

                        if (config.getStatus().getState() == State.FINAL_ERROR ||
                                config.getStatus().getState() == State.ACTIVE) {
                            logger.info("SSO provisioning {}", config.getStatus().getState());
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
            logger.warn("SSO provisioning interrupted", e);
        }
    }

    private void handleStateTransition(Config config) throws InterruptedException, IOException {
        switch (config.getStatus().getState()) {
            case IN_ACTIVATION, IN_SELF_RECOVERABLE_ERROR, IN_RECOVERABLE_ERROR -> {
                config.getStatus().setState(State.ACTIVE);
                this.repo.save(config, this.TenantId, this.fileId);
            }
            case IN_DELETION -> {
                logger.info("SSO deletion detected");
                this.repo.deleteByTenantIdAndSsoId(this.TenantId, config.getId());
            }
            default -> {
                logger.warn("Unhandled state: {}", config.getStatus().getState());
            }
        }

    }


}
