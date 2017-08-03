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
import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.sync.SyncAdapter;
import com.dsr_practice.car_workshop.sync.SyncInterface;

import java.util.ArrayList;
import java.util.List;

public class JobList implements SyncInterface {
    private List<Job> jobs;

    private static final String[] JOB_PROJECTION = new String[] {
            Contract.JobEntry._ID,
            Contract.JobEntry.COLUMN_NAME_JOB_ID,
            Contract.JobEntry.COLUMN_NAME_JOB_NAME,
            Contract.JobEntry.COLUMN_NAME_PRICE
    };

    public JobList(List<Job> jobs) {
        this.jobs = jobs;
    }

    @Override
    public void sync(ContentResolver contentResolver, SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // Build hash table of incoming jobs
        SparseArray<Job> entryArray = new SparseArray<>();
        for (Job job : jobs) {
            entryArray.put(job.getId(), job);

        }

        // Get all jobs in local database to compare with data from server
        Cursor cursor = contentResolver.query(
                Contract.JobEntry.CONTENT_URI, JOB_PROJECTION, null, null, null);
        assert cursor != null;

        // Cursor fields
        int id, jobId, price;
        String name;
        while (cursor.moveToNext()) {
            syncResult.stats.numEntries++;
            id = cursor.getInt(SyncAdapter.COLUMN_ID);
            jobId = cursor.getInt(SyncAdapter.COLUMN_ENTRY_ID);
            name = cursor.getString(SyncAdapter.COLUMN_ENTRY_NAME);
            price = cursor.getInt(SyncAdapter.COLUMN_OPTIONAL_FIELD);

            Job match = entryArray.get(jobId);
            if (match != null) {
                // Job exists. Remove from entry map to prevent insert later
                entryArray.remove(jobId);
                // Check to see if the job needs to be updated
                Uri uri = Contract.JobEntry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if (match.getName() != null && !match.getName().equals(name) ||
                        match.getPrice() != price) {
                    // Update existing record
                    batch.add(ContentProviderOperation.newUpdate(uri)
                            .withValue(Contract.JobEntry.COLUMN_NAME_JOB_NAME, match.getName())
                            .withValue(Contract.JobEntry.COLUMN_NAME_PRICE, match.getPrice())
                            .build());
                    syncResult.stats.numUpdates++;
                }
                else {
                    // Job doesn't exist anymore. Remove it from the database
                    batch.add(ContentProviderOperation.newDelete(uri).build());
                    syncResult.stats.numDeletes++;
                }
            }
        } // while
        cursor.close();

        // Add new models
        for (int i = 0; i < entryArray.size(); i++) {
            int key = entryArray.keyAt(i);
            Job job = entryArray.get(key);
            batch.add(ContentProviderOperation.newInsert(Contract.JobEntry.CONTENT_URI)
                    .withValue(Contract.JobEntry.COLUMN_NAME_JOB_ID, job.getId())
                    .withValue(Contract.JobEntry.COLUMN_NAME_JOB_NAME, job.getName())
                    .withValue(Contract.JobEntry.COLUMN_NAME_PRICE, job.getPrice())
                    .build());
            syncResult.stats.numInserts++;
        }
        contentResolver.applyBatch(Contract.CONTENT_AUTHORITY, batch);
        contentResolver.notifyChange(
                Contract.JobEntry.CONTENT_URI,  // URI where data was modified
                null,                           // No local observer
                false);
    }
}
