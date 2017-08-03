package com.dsr_practice.car_workshop.sync;

import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.RemoteException;

public interface SyncInterface {
    void sync(ContentResolver contentResolver, final SyncResult syncResult)
            throws RemoteException, OperationApplicationException;
}
