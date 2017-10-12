package com.dsr_practice.car_workshop.models.common.sync;

import java.io.Serializable;

public class SyncModel implements Serializable {

    protected int id;

    protected String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
