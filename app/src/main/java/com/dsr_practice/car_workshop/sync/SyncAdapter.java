package com.dsr_practice.car_workshop.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Toast;

import com.dsr_practice.car_workshop.accounts.AccountGeneral;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.sync.Job;
import com.dsr_practice.car_workshop.models.common.sync.Mark;
import com.dsr_practice.car_workshop.models.common.sync.Model;
import com.dsr_practice.car_workshop.models.lists.JobList;
import com.dsr_practice.car_workshop.models.lists.MarkList;
import com.dsr_practice.car_workshop.models.lists.ModelList;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    // Content resolver, for performing database operations
    private final ContentResolver contentResolver;

    private static ApiInterface apiInterface;

    //region Constants representing column positions from every PROJECTION
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;
    public static final int COLUMN_ENTRY_NAME = 2;
    public static final int COLUMN_OPTIONAL_FIELD = 3;
    //endregion

    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
        apiInterface = ApiClient.getApi();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        contentResolver = context.getContentResolver();
    }

    /* Merge strategy:
     * 1. Get cursor to all items
     * 2. For each item, check if it's in the incoming data
     *    a. YES: Remove from "incoming" list.
     *            Check if data has mutated, if so, perform database UPDATE
     *    b. NO: Schedule DELETE from database
     * (At this point, incoming list only contains missing items)
     * 3. For any items remaining in incoming list, ADD to database
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, final SyncResult syncResult) {
        // Sync mark list
        apiInterface.getMarks().enqueue(new Callback<List<Mark>>() {
            @Override
            public void onResponse(Call<List<Mark>> call, Response<List<Mark>> response) {
                try {
                    new MarkList(response.body()).sync(contentResolver, syncResult);
                } catch (RemoteException | OperationApplicationException e) {
                    syncResult.databaseError = true;
                }
            }

            @Override
            public void onFailure(Call<List<Mark>> call, Throwable t) {
                Toast.makeText(getContext(), "Fail to sync mark list with server", Toast.LENGTH_SHORT).show();
            }
        });

        // Sync model list
        apiInterface.getModels().enqueue(new Callback<List<Model>>() {
            @Override
            public void onResponse(Call<List<Model>> call, Response<List<Model>> response) {
                try {
                    new ModelList(response.body()).sync(contentResolver, syncResult);
                } catch (RemoteException | OperationApplicationException e) {
                    syncResult.databaseError = true;
                }
            }

            @Override
            public void onFailure(Call<List<Model>> call, Throwable t) {
                Toast.makeText(getContext(), "Fail to sync model list with server", Toast.LENGTH_SHORT).show();
            }
        });

        // Sync job list
        apiInterface.getJobs().enqueue(new Callback<List<Job>>() {
            @Override
            public void onResponse(Call<List<Job>> call, Response<List<Job>> response) {
                try {
                    new JobList(response.body()).sync(contentResolver, syncResult);
                } catch (RemoteException | OperationApplicationException e) {
                    syncResult.databaseError = true;
                }
            }

            @Override
            public void onFailure(Call<List<Job>> call, Throwable t) {
                Toast.makeText(getContext(), "Fail to sync job list with server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    //region Sync local database with server
    private void syncMarkList(List<Mark> newList, final SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // Build hash table of incoming marks
        SparseArray<Mark> entryArray = new SparseArray<>();
        for (Mark mark : newList) {
            entryArray.put(mark.getId(), mark);
        }

        // Get all marks in local database to compare with data from server
        Cursor cursor = contentResolver.query(Contract.MarkEntry.CONTENT_URI, MARK_PROJECTION, null, null, null);
        assert cursor != null;

        // Cursor fields
        int id, markId;
        String name;
        while (cursor.moveToNext()) {
            syncResult.stats.numEntries++;
            id = cursor.getInt(COLUMN_ID);
            markId = cursor.getInt(COLUMN_ENTRY_ID);
            name = cursor.getString(COLUMN_ENTRY_NAME);

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

    private void syncModelList(List<Model> newList, SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // Build hash table of incoming models
        SparseArray<Model> entryArray = new SparseArray<>();
        for (Model model : newList) {
            entryArray.put(model.getId(), model);
        }

        // Get all models in local database to compare with data from server
        Cursor cursor = contentResolver.query(Contract.ModelEntry.CONTENT_URI, MODEL_PROJECTION, null, null, null);
        assert cursor != null;

        // Cursor fields
        int id, modelId;
        String name;
        while (cursor.moveToNext()) {
            syncResult.stats.numEntries++;
            id = cursor.getInt(COLUMN_ID);
            modelId = cursor.getInt(COLUMN_ENTRY_ID);
            name = cursor.getString(COLUMN_ENTRY_NAME);

            Model match = entryArray.get(modelId);
            if (match != null) {
                // Model exists. Remove from entry map to prevent insert later
                entryArray.remove(modelId);
                // Check to see if the model needs to be updated
                Uri uri = Contract.ModelEntry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if (match.getName() != null && !match.getName().equals(name)) {
                    // Update existing record
                    batch.add(ContentProviderOperation.newUpdate(uri)
                            .withValue(Contract.ModelEntry.COLUMN_NAME_MODEL_NAME, match.getName())
                            .build());
                    syncResult.stats.numUpdates++;
                }
                else {
                    // Model doesn't exist anymore. Remove it from the database
                    batch.add(ContentProviderOperation.newDelete(uri).build());
                    syncResult.stats.numDeletes++;
                }
            }
        } // while
        cursor.close();

        // Add new models
        for (int i = 0; i < entryArray.size(); i++) {
            int key = entryArray.keyAt(i);
            Model model = entryArray.get(key);
            batch.add(ContentProviderOperation.newInsert(Contract.ModelEntry.CONTENT_URI)
                    .withValue(Contract.ModelEntry.COLUMN_NAME_MODEL_ID, model.getId())
                    .withValue(Contract.ModelEntry.COLUMN_NAME_MODEL_NAME, model.getName())
                    .withValue(Contract.ModelEntry.COLUMN_NAME_FK_MARK_ID, model.getMark())
                    .build());
            syncResult.stats.numInserts++;
        }
        contentResolver.applyBatch(Contract.CONTENT_AUTHORITY, batch);
        contentResolver.notifyChange(
                Contract.ModelEntry.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);
    }

    private void syncJobList(List<Job> newList, SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // Build hash table of incoming jobs
        SparseArray<Job> entryArray = new SparseArray<>();
        for (Job job : newList) {
            entryArray.put(job.getId(), job);

        }

        // Get all jobs in local database to compare with data from server
        Cursor cursor = contentResolver.query(Contract.JobEntry.CONTENT_URI, JOB_PROJECTION, null, null, null);
        assert cursor != null;

        // Cursor fields
        int id, jobId, price;
        String name;
        while (cursor.moveToNext()) {
            syncResult.stats.numEntries++;
            id = cursor.getInt(COLUMN_ID);
            jobId = cursor.getInt(COLUMN_ENTRY_ID);
            name = cursor.getString(COLUMN_ENTRY_NAME);
            price = cursor.getInt(COLUMN_OPTIONAL_FIELD);

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
    //endregion
    */

    // Manual force Android to perform a sync with SyncAdapter
    public static void performSync() {
        Bundle bundle = new Bundle();
        // Disable sync backoff and ignore sync preferences
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AccountGeneral.getAccount(),
                Contract.CONTENT_AUTHORITY, bundle);
    }
}
