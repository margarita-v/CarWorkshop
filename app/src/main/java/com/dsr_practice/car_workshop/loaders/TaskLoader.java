package com.dsr_practice.car_workshop.loaders;

import android.content.Context;

import com.dsr_practice.car_workshop.models.common.Task;

import java.util.List;

public class TaskLoader extends BaseLoader<List<Task>> {

    public static final int TASK_LOADER_ID = 1;

    public TaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        apiInterface.getTasks().enqueue(baseCallbacks);
    }
}
