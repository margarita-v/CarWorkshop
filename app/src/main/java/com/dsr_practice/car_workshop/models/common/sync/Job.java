package com.dsr_practice.car_workshop.models.common.sync;

public class Job extends SyncModel {

    private int price;

    public Job(int id, int price, String name) {
        this.id = id;
        this.price = price;
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public String getPriceToString() {
        return Integer.toString(price);
    }
}
