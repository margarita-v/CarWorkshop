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

import com.dsr_practice.car_workshop.accounts.AccountGeneral;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.sync.Job;
import com.dsr_practice.car_workshop.models.common.sync.Mark;
import com.dsr_practice.car_workshop.models.common.sync.Model;
import com.dsr_practice.car_workshop.models.common.sync.SyncModel;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;

/**
 * Class which will perform sync process
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    // Content resolver, for performing database operations
    private final ContentResolver contentResolver;

    // Interface for REST usage
    private static ApiInterface apiInterface;

    // Flag for sync cancel
    private boolean isSyncCanceled;

    //region Objects for sync of all items
    private SyncJobs syncJobs;
    private SyncMarks syncMarks;
    private SyncModels syncModels;
    //endregion

    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
        apiInterface = ApiClient.getApi();

        syncJobs = new SyncJobs();
        syncMarks = new SyncMarks();
        syncModels = new SyncModels();
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
    public void onPerformSync(final Account account, Bundle extras, String authority,
                              ContentProviderClient provider, final SyncResult syncResult) {

        syncJobs.performSync(Job.JOB_ID, syncResult);

        if (!isSyncCanceled) {
            syncMarks.performSync(Mark.MARK_ID, syncResult);

            if (!isSyncCanceled)
                syncModels.performSync(Model.MODEL_ID, syncResult);
            else
                ContentResolver.cancelSync(account, authority);
        }
        else
            ContentResolver.cancelSync(account, authority);
    }

    /**
     * Common class for sync of all entries
     * @param <T> Type of entry which will be loaded from server
     */
    private abstract class EntrySync<T extends SyncModel> {

        /**
         * Abstract method for response which depends on the entry's type
         */
        abstract Call<List<T>> getResponse() throws IOException;

        /**
         * Perform sync of concrete entry
         * @param entryId ID of entry which will be synced
         * @param syncResult SyncResult for counting of applied changes during the sync process
         */
        void performSync(int entryId, SyncResult syncResult) {
            try {
                //SyncAdapter will run in its own thread so we don't need to create
                // another thread for requests to the server
                List<T> responseBody = getResponse().execute().body();
                if (responseBody != null) {
                    new SyncImplementation<>(responseBody, entryId)
                            .sync(contentResolver, syncResult);
                }
                else
                    // Response is null; server is unavailable
                    isSyncCanceled = true;
            } catch (RemoteException | OperationApplicationException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    //region Classes for sync of concrete entries
    private class SyncJobs extends EntrySync<Job> {

        @Override
        Call<List<Job>> getResponse() throws IOException {
            return apiInterface.getJobs();
        }
    }

    private class SyncMarks extends EntrySync<Mark> {

        @Override
        Call<List<Mark>> getResponse() throws IOException {
            return apiInterface.getMarks();
        }
    }

    private class SyncModels extends EntrySync<Model> {

        @Override
        Call<List<Model>> getResponse() throws IOException {
            return apiInterface.getModels();
        }
    }
    //endregion

    /**
     * Manual force Android to perform a sync with SyncAdapter
     */
    public static void performSync() {
        Bundle bundle = new Bundle();
        // Disable sync backoff and ignore sync preferences
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AccountGeneral.getAccount(),
                Contract.CONTENT_AUTHORITY, bundle);
    }
}
