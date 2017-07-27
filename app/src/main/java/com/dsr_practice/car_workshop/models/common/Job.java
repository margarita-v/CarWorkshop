package com.dsr_practice.car_workshop.models.common;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Job implements Serializable {
    private int id;
    private int price;
    @SerializedName("job_name")
    private String name;

    public Job(int id, int price, String name) {
        this.id = id;
        this.price = price;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public String getPriceToString() {
        return Integer.toString(price);
    }

    public String getName() {
        return name;
    }
}
