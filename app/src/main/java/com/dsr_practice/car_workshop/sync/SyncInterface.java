package com.dsr_practice.car_workshop.sync;

import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.RemoteException;

/**
 * Interface which declare a main method for sync
 */
interface SyncInterface {

    void sync(ContentResolver contentResolver, final SyncResult syncResult)
            throws RemoteException, OperationApplicationException;
}
