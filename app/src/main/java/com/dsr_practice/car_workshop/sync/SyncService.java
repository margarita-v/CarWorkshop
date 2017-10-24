package com.dsr_practice.car_workshop.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Class for sync adapter control
 */
public class SyncService extends Service {

    private static final Object LOCK = new Object();
    private static SyncAdapter syncAdapter = null;

    // Thread-safe constructor, creates static SyncAdapter instance.
    @Override
    public void onCreate() {
        synchronized (LOCK) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    // Return our SyncAdapter's IBinder
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
