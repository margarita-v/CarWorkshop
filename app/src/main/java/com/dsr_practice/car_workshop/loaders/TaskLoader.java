package com.dsr_practice.car_workshop.loaders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.rest.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskLoader extends Loader<List<Task>> {

    private Call<List<Task>> callTasks;

    @Nullable
    private List<Task> taskList;

    public TaskLoader(Context context) {
        super(context);
        callTasks = ApiClient.getApi().getTasks();
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (taskList != null)
            deliverResult(taskList);
        else
            forceLoad();
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        callTasks.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                taskList = response.body();
                deliverResult(taskList);
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                deliverResult(null);
            }
        });
    }

    @Override
    protected void onStopLoading() {
        callTasks.cancel();
        super.onStopLoading();
    }
}
