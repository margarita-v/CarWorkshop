package com.dsr_practice.car_workshop.models.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.dsr_practice.car_workshop.models.common.sync.Job;

import java.io.Serializable;

public class JobStatus implements Parcelable {
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

    protected JobStatus(Parcel in) {
        id = in.readInt();
        task = in.readInt();
        job = in.readParcelable(Job.class.getClassLoader());
        status = in.readInt() != 0;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(task);
        parcel.writeParcelable(job, i);
        parcel.writeInt(status ? 1 : 0);
    }

    public static final Creator<JobStatus> CREATOR = new Creator<JobStatus>() {
        @Override
        public JobStatus createFromParcel(Parcel in) {
            return new JobStatus(in);
        }

        @Override
        public JobStatus[] newArray(int size) {
            return new JobStatus[size];
        }
    };
}
