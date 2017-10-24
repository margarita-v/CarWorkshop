package com.dsr_practice.car_workshop.loaders;

import android.content.Context;

import com.dsr_practice.car_workshop.models.post.TaskPost;

import okhttp3.ResponseBody;

public class CreateTaskLoader extends BaseLoader<ResponseBody> {

    public static final int CREATE_TASK_ID = 4;

    private TaskPost taskPostObject;

    public CreateTaskLoader(Context context, TaskPost taskPostObject) {
        super(context);
        this.taskPostObject = taskPostObject;
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        apiInterface.createTask(taskPostObject).enqueue(baseCallbacks);
    }
}
