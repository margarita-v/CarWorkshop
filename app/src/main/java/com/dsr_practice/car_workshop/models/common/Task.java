package com.dsr_practice.car_workshop.models.common;

import java.util.Date;
import java.util.List;

public class Task {
    private int id;
    private Date date;
    private int model;
    private int mark;
    private String number;
    private String vin;
    private String name;
    private boolean status;
    private List<JobStatus> jobs;

    public Task(int id, Date date, int model, int mark, String number, String vin, String name, boolean status) {
        this.id = id;
        this.date = date;
        this.model = model;
        this.mark = mark;
        this.number = number;
        this.vin = vin;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public int getModel() {
        return model;
    }

    public int getMark() {
        return mark;
    }

    public String getNumber() {
        return number;
    }

    public String getVin() {
        return vin;
    }

    public String getName() {
        return name;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<JobStatus> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobStatus> jobs) {
        this.jobs = jobs;
    }
}
