package com.dsr_practice.car_workshop.models.common;

import com.google.gson.annotations.SerializedName;

public class Mark {
    private int id;
    @SerializedName("mark_name")
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
