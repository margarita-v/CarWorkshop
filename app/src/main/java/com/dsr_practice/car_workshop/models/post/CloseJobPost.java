package com.dsr_practice.car_workshop.models.post;

import com.google.gson.annotations.SerializedName;

public class CloseJobPost {
    @SerializedName("task_id")
    private int taskId;

    @SerializedName("job_id")
    private int jobId;

    public CloseJobPost(int taskId, int jobId) {
        this.taskId = taskId;
        this.jobId = jobId;
    }
}
