package com.dsr_practice.car_workshop.models.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.dsr_practice.car_workshop.models.common.sync.Job;

public class JobStatus implements Parcelable {
    private int id;
    private int task;
    private Job job;
    private boolean status;

    //region Parcelable implementation
    private JobStatus(Parcel in) {
        id = in.readInt();
        task = in.readInt();
        job = in.readParcelable(Job.class.getClassLoader());
        status = in.readByte() != 0;
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
        parcel.writeByte((byte) (status ? 1 : 0));
    }

    static final Creator<JobStatus> CREATOR = new Creator<JobStatus>() {
        @Override
        public JobStatus createFromParcel(Parcel in) {
            return new JobStatus(in);
        }

        @Override
        public JobStatus[] newArray(int size) {
            return new JobStatus[size];
        }
    };
    //endregion

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
}
