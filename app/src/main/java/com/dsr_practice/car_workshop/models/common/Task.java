package com.dsr_practice.car_workshop.models.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Task extends ExpandableGroup<JobStatus> implements Parcelable {
    private int id;
    private Date date;
    private int model;
    private int mark;
    private String number;
    private String vin;
    private String name;
    private boolean status;
    private List<JobStatus> jobs;

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(
            DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

    public Task(int id, Date date, int model, int mark,
                String number, String vin, String name,
                boolean status, List<JobStatus> jobs) {
        super(name, jobs);
        this.id = id;
        this.date = date;
        this.model = model;
        this.mark = mark;
        this.number = number;
        this.vin = vin;
        this.name = name;
        this.status = status;
        this.jobs = new ArrayList<>(jobs);
    }

    //region Parcelable implementation
    protected Task(Parcel in) {
        super(in);
        id = in.readInt();
        date = (Date) in.readSerializable();
        model = in.readInt();
        mark = in.readInt();
        number = in.readString();
        vin = in.readString();
        name = in.readString();
        status = in.readByte() != 0;
        jobs = in.createTypedArrayList(JobStatus.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeSerializable(date);
        dest.writeInt(model);
        dest.writeInt(mark);
        dest.writeString(number);
        dest.writeString(vin);
        dest.writeString(name);
        dest.writeByte((byte) (status ? 1 : 0));
        dest.writeTypedList(jobs);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
    //endregion

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getDateToString() {
        return DATE_FORMAT.format(date);
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

    public int getFullPrice() {
        int price = 0;
        for (JobStatus jobStatus: getJobs()) {
            price += jobStatus.getJob().getPrice();
        }
        return price;
    }
}
