package com.dsr_practice.car_workshop.models;

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

    public List<JobStatus> getJobs() {
        return jobs;
    }
}
