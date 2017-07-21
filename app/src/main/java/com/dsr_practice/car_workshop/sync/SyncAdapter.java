package com.dsr_practice.car_workshop.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.widget.Toast;

import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.models.common.Mark;
import com.dsr_practice.car_workshop.models.common.Model;
import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SYNC_ADAPTER";

    // Content resolver, for performing database operations
    private final ContentResolver contentResolver;

    private static ApiInterface apiInterface;

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
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        // Sync mark list
        apiInterface.getMarks().enqueue(new Callback<List<Mark>>() {
            @Override
            public void onResponse(Call<List<Mark>> call, Response<List<Mark>> response) {
                syncMarkList(response.body());
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
                syncModelList(response.body());
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
                syncJobList(response.body());
            }

            @Override
            public void onFailure(Call<List<Job>> call, Throwable t) {
                Toast.makeText(getContext(), "Fail to sync job list with server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //region Sync local database with server
    private void syncMarkList(List<Mark> newList) {

    }

    private void syncModelList(List<Model> newList) {

    }

    private void syncJobList(List<Job> newList) {

    }
    //endregion
}
