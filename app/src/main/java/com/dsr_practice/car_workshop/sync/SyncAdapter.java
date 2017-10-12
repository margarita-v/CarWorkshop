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

/**
 * Class which will perform sync process
 */
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
