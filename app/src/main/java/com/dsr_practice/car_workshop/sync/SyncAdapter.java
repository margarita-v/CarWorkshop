package com.dsr_practice.car_workshop.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.SparseArray;
import android.widget.Toast;

import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.models.common.Mark;
import com.dsr_practice.car_workshop.models.common.Model;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SYNC_ADAPTER";

    // Content resolver, for performing database operations
    private final ContentResolver contentResolver;

    private static ApiInterface apiInterface;

    //region Projections for queries
    private static final String[] MARK_PROJECTION = new String[] {
            Contract.MarkEntry._ID,
            Contract.MarkEntry.COLUMN_NAME_MARK_ID,
            Contract.MarkEntry.COLUMN_NAME_MARK_NAME
    };
    private static final String[] MODEL_PROJECTION = new String[] {
            Contract.ModelEntry._ID,
            Contract.ModelEntry.COLUMN_NAME_MODEL_ID,
            Contract.ModelEntry.COLUMN_NAME_MODEL_NAME,
            Contract.ModelEntry.COLUMN_NAME_FK_MARK_ID
    };
    private static final String[] JOB_PROJECTION = new String[] {
            Contract.JobEntry._ID,
            Contract.JobEntry.COLUMN_NAME_JOB_ID,
            Contract.JobEntry.COLUMN_NAME_JOB_NAME,
            Contract.JobEntry.COLUMN_NAME_PRICE
    };
    //endregion

    //region Constants representing column positions from every PROJECTION
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_ENTRY_ID = 1;
    private static final int COLUMN_ENTRY_NAME = 2;
    private static final int COLUMN_OPTIONAL_FIELD = 3;
    //endregion

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
        apiInterface = ApiClient.getApi();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        contentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, final SyncResult syncResult) {
        // Sync mark list
        apiInterface.getMarks().enqueue(new Callback<List<Mark>>() {
            @Override
            public void onResponse(Call<List<Mark>> call, Response<List<Mark>> response) {
                try {
                    syncMarkList(response.body(), syncResult);
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
                syncModelList(response.body(), syncResult);
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
                syncJobList(response.body(), syncResult);
            }

            @Override
            public void onFailure(Call<List<Job>> call, Throwable t) {
                Toast.makeText(getContext(), "Fail to sync job list with server", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
        int id, entryId;
        String name;
        while (cursor.moveToNext()) {
            syncResult.stats.numEntries++;
            id = cursor.getInt(COLUMN_ID);
            entryId = cursor.getInt(COLUMN_ENTRY_ID);
            name = cursor.getString(COLUMN_ENTRY_NAME);

            Mark match = entryArray.get(entryId);
            if (match != null) {
                // Mark exists. Remove from entry map to prevent insert later
                entryArray.remove(entryId);
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

    private void syncModelList(List<Model> newList, SyncResult syncResult) {

    }

    private void syncJobList(List<Job> newList, SyncResult syncResult) {

    }
    //endregion
}
