package com.dsr_practice.car_workshop.models.common;

import com.dsr_practice.car_workshop.models.common.sync.Job;

import java.io.Serializable;

public class JobStatus implements Serializable {
    private int id;
    private int task;
    private Job job;
    private boolean status;

    public JobStatus(int id, int task, Job job, boolean status) {
        this.id = id;
        this.task = task;
        this.job = job;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getTask() {
        return task;
    }

    public Job getJob() {
        return job;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
