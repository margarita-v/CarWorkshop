package com.dsr_practice.car_workshop.models.common;

import com.google.gson.annotations.SerializedName;

public class Job {
    private int id;
    private int price;
    @SerializedName("job_name")
    private String name;

    public int getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }
}
