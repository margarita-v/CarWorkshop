package com.dsr_practice.car_workshop.models.common;

import com.google.gson.annotations.SerializedName;

public class Model {
    private int id;
    private int mark;
    @SerializedName("model_name")
    private String name;

    public int getId() {
        return id;
    }

    public int getMark() {
        return mark;
    }

    public String getName() {
        return name;
    }
}
