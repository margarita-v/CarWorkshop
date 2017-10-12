package com.dsr_practice.car_workshop.models.lists;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.SparseArray;

import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.sync.Mark;
import com.dsr_practice.car_workshop.sync.SyncAdapter;
import com.dsr_practice.car_workshop.sync.SyncInterface;

import java.util.ArrayList;
import java.util.List;

public class MarkList implements SyncInterface {
    private List<Mark> marks;

    private static final String[] MARK_PROJECTION = new String[] {
            Contract.MarkEntry._ID,
            Contract.MarkEntry.COLUMN_NAME_MARK_ID,
            Contract.MarkEntry.COLUMN_NAME_MARK_NAME
    };

    public MarkList(List<Mark> marks) {
        this.marks = marks;
    }

    @Override
    public void sync(ContentResolver contentResolver, final SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // Build hash table of incoming marks
        SparseArray<Mark> entryArray = new SparseArray<>();
        for (Mark mark : marks) {
            entryArray.put(mark.getId(), mark);
        }

        // Get all marks in local database to compare with data from server
        Cursor cursor = contentResolver.query(
                Contract.MarkEntry.CONTENT_URI, MARK_PROJECTION, null, null, null);
        assert cursor != null;

        // Cursor fields
        int id, markId;
        String name;
        while (cursor.moveToNext()) {
            syncResult.stats.numEntries++;
            id = cursor.getInt(SyncAdapter.COLUMN_ID);
            markId = cursor.getInt(SyncAdapter.COLUMN_ENTRY_ID);
            name = cursor.getString(SyncAdapter.COLUMN_ENTRY_NAME);

            Mark match = entryArray.get(markId);
            if (match != null) {
                // Mark exists. Remove from entry map to prevent insert later
                entryArray.remove(markId);
                // Check to see if the mark needs to be updated
                Uri uri = Contract.MarkEntry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if (match.getName() != null && !match.getName().equals(name)) {
                    // Update existing record
                    batch.add(ContentProviderOperation.newUpdate(uri)
                            .withValue(Contract.MarkEntry.COLUMN_NAME_MARK_NAME, match.getName())
                            .build());
                    syncResult.stats.numUpdates++;
                }
                else {
                    // Mark doesn't exist anymore. Remove it from the database
                    batch.add(ContentProviderOperation.newDelete(uri).build());
                    syncResult.stats.numDeletes++;
                }
            }
        } // while
        cursor.close();

        // Add new marks
        for (int i = 0; i < entryArray.size(); i++) {
            int key = entryArray.keyAt(i);
            Mark mark = entryArray.get(key);
            batch.add(ContentProviderOperation.newInsert(Contract.MarkEntry.CONTENT_URI)
                    .withValue(Contract.MarkEntry.COLUMN_NAME_MARK_ID, mark.getId())
                    .withValue(Contract.MarkEntry.COLUMN_NAME_MARK_NAME, mark.getName())
                    .build());
            syncResult.stats.numInserts++;
        }
        contentResolver.applyBatch(Contract.CONTENT_AUTHORITY, batch);
        contentResolver.notifyChange(
                Contract.MarkEntry.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);
    }
}
