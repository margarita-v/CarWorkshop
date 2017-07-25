package com.dsr_practice.car_workshop.models.common;

import java.io.Serializable;

public class JobStatus implements Serializable {
    private int id;
    private int task;
    private int job;
    private boolean status;

    public int getId() {
        return id;
    }

    public int getTask() {
        return task;
    }

    public int getJob() {
        return job;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
