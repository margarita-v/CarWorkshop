package com.dsr_practice.car_workshop.models.common.sync;

import java.io.Serializable;

/**
 * Base class for all items which will be synced with server and stored in local database
 */
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
