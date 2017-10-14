package com.dsr_practice.car_workshop.models.common.sync;

import android.os.Parcel;
import android.os.Parcelable;

public class Job extends SyncModel implements Parcelable {

    public static final int JOB_ID = 0;

    private int price;

    public Job(int id, int price, String name) {
        this.id = id;
        this.price = price;
        this.name = name;
    }

    //region Parcelable implementation
    protected Job(Parcel in) {
        id = in.readInt();
        name = in.readString();
        price = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeInt(price);
    }

    public static final Creator<Job> CREATOR = new Creator<Job>() {
        @Override
        public Job createFromParcel(Parcel in) {
            return new Job(in);
        }

        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };
    //endregion

    public int getPrice() {
        return price;
    }

    public String getPriceToString() {
        return Integer.toString(price);
    }
}
