package com.dsr_practice.car_workshop.loaders;

import android.content.Context;

import com.dsr_practice.car_workshop.models.common.Task;

import okhttp3.ResponseBody;

public class CloseTaskLoader extends BaseLoader<Task> {

    public static final int CLOSE_TASK_ID = 2;

    private int taskId;

    public CloseTaskLoader(Context context, int taskId) {
        super(context);
        this.taskId = taskId;
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        apiInterface.closeTask(taskId).enqueue(baseCallbacks);
    }
}
