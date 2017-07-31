package com.dsr_practice.car_workshop.models.post;

import com.dsr_practice.car_workshop.models.common.Job;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class TaskPost {
    @SerializedName("mark")
    private int markId;

    @SerializedName("model")
    private int modelId;

    private String date;

    private String vin;

    private String number;

    private List<Job> jobs;

    public TaskPost(int markId, int modelId, String date, String vin, String number, List<Job> jobs) {
        this.markId = markId;
        this.modelId = modelId;
        this.date = date;
        this.vin = vin;
        this.number = number;
        this.jobs = jobs;
    }
}
