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

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.accounts.AccountGeneral;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.sync.Job;
import com.dsr_practice.car_workshop.models.common.sync.Mark;
import com.dsr_practice.car_workshop.models.common.sync.Model;
import com.dsr_practice.car_workshop.models.common.sync.SyncModel;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class which will perform sync process
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    // Content resolver, for performing database operations
    private final ContentResolver contentResolver;

    // Interface for REST usage
    private static ApiInterface apiInterface;

    //region IDs of string resources for different errors messages
    private static final int JOB_SYNC_ERROR = R.string.sync_error_jobs;
    private static final int MARK_SYNC_ERROR = R.string.sync_error_marks;
    private static final int MODEL_SYNC_ERROR = R.string.sync_error_models;
    //endregion

    //region Variables for SyncCallback class usage
    private int currentEntryId;
    private SyncResult syncResult;
    private int stringResourceId;
    //endregion

    //region Constructors
    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
        apiInterface = ApiClient.getApi();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        contentResolver = context.getContentResolver();
        apiInterface = ApiClient.getApi();
    }
    //endregion

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
        this.syncResult = syncResult;

        // Sync job list
        this.currentEntryId = Job.JOB_ID;
        this.stringResourceId = JOB_SYNC_ERROR;

        apiInterface.getJobs().enqueue(new SyncCallback<Job>());

        // Sync mark list
        this.currentEntryId = Mark.MARK_ID;
        this.stringResourceId = MARK_SYNC_ERROR;

        apiInterface.getMarks().enqueue(new SyncCallback<Mark>());

        // Sync model list
        this.currentEntryId = Model.MODEL_ID;
        this.stringResourceId = MODEL_SYNC_ERROR;

        apiInterface.getModels().enqueue(new SyncCallback<Model>());
    }

    /**
     * Generic class for implementing callbacks for data sync
     * @param <T> Type of entry which will be loaded
     */
    private class SyncCallback<T extends SyncModel> implements Callback<List<T>> {

        @Override
        public void onResponse(Call<List<T>> call, Response<List<T>> response) {
            try {
                new SyncImplementation<>(response.body(), currentEntryId)
                        .sync(contentResolver, syncResult);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<List<T>> call, Throwable t) {
            Toast.makeText(getContext(), stringResourceId, Toast.LENGTH_SHORT).show();
        }
    }

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
