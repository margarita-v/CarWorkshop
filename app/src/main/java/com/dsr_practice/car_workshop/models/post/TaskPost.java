package com.dsr_practice.car_workshop.models.post;

import com.dsr_practice.car_workshop.models.common.Job;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class TaskPost {
    @SerializedName("mark")
    private int markId;

    @SerializedName("mark_name")
    private String markName;

    @SerializedName("model")
    private int modelId;

    private Date date;

    private String vin;

    private String number;

    private List<Job> jobs;
}
