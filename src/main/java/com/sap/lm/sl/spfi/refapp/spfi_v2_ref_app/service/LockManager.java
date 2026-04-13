package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockManager {


    private final ConcurrentHashMap<String, LockEntry> lockMap = new ConcurrentHashMap<>();
    private static final long INACTIVITY_TIMEOUT = 60000L; // 1 minute inactivity timeout (adjust as needed)


    private static class LockEntry {
        Lock lock;
        long lastActiveTimestamp;

        LockEntry(Lock lock) {
            this.lock = lock;
            this.lastActiveTimestamp = System.currentTimeMillis();
        }
    }


    // Get or create a lock by its name
    public Lock getLock(String lockName) {
        lockMap.putIfAbsent(lockName, new LockEntry(new ReentrantLock()));
        LockEntry entry = lockMap.get(lockName);
        entry.lastActiveTimestamp = System.currentTimeMillis(); // Update activity timestamp
        return entry.lock;
    }

    // Scheduled task to periodically clean up inactive locks
    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void cleanupInactiveLocks() {
        long currentTime = System.currentTimeMillis();
        lockMap.entrySet().removeIf(entry -> currentTime - entry.getValue().lastActiveTimestamp > INACTIVITY_TIMEOUT);
    }


    public void manualCleanup() {
        cleanupInactiveLocks();
    }
}

