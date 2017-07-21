package com.dsr_practice.car_workshop.models.post;

import com.google.gson.annotations.SerializedName;

public class CloseJobPost {
    @SerializedName("task_id")
    private int taskId;

    @SerializedName("job_id")
    private int jobId;
}
