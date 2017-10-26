package com.dsr_practice.car_workshop.loaders;

import android.content.Context;

import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.models.post.CloseJobPost;

public class CloseJobLoader extends BaseLoader<Task> {

    public static final int CLOSE_JOB_ID = 3;

    private int taskId, jobId;

    public CloseJobLoader(Context context, int taskId, int jobId) {
        super(context);
        this.taskId = taskId;
        this.jobId = jobId;
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        apiInterface.closeJobInTask(new CloseJobPost(taskId, jobId)).enqueue(baseCallbacks);
    }
}
