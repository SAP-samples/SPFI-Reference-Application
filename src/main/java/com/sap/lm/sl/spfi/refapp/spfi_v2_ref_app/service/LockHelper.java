package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.service;

import java.util.concurrent.locks.Lock;

public class LockHelper {

    private final LockManager lockManager;
    private final String key;

    public LockHelper(LockManager lockManager, String key) {
        this.lockManager = lockManager;
        this.key = key;
    }

    public Lock getLock() {
        return lockManager.getLock(key);
    }

}
