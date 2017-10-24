package com.dsr_practice.car_workshop.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;

import com.dsr_practice.car_workshop.R;

import java.util.Arrays;

public class JobAdapter extends SimpleCursorAdapter {

    // All jobs positions
    private boolean[] checkedPositions;

    // Count of checked jobs
    private int checkedCount;

    public JobAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        int count = c != null ? c.getCount() : 0;
        this.checkedPositions = new boolean[count];
        this.checkedCount = 0;
    }

    @NonNull
    @Override
    public Cursor swapCursor(@NonNull Cursor cursor) {
        this.checkedPositions = new boolean[cursor.getCount()];
        this.checkedCount = 0;
        return super.swapCursor(cursor);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.job_item, parent, false);
        }
        CheckBox cbName = convertView.findViewById(R.id.cbName);
        cbName.setChecked(this.checkedPositions[position]);
        return super.getView(position, convertView, parent);
    }

    //region Getters and setters
    public boolean[] getCheckedPositions() {
        return checkedPositions;
    }

    public int getSize() {
        return checkedPositions.length;
    }

    public int getCheckedCount() {
        return checkedCount;
    }

    public void setCheckedPositions(boolean[] checkedPositions) {
        this.checkedPositions = Arrays.copyOf(checkedPositions, checkedPositions.length);
        checkedCount = 0;
        for (boolean checked: checkedPositions) {
            if (checked)
                checkedCount++;
        }
        notifyDataSetChanged();
    }
    //endregion

    /**
     * Get a state of chosen job
     * @param position Job position which state will be checked
     * @return True if job is checked
     */
    public boolean isChecked(int position) {
        return checkedPositions[position];
    }

    /**
     * Check or uncheck job
     * @param position Job position which state will be changed
     * @return Job state: True if job is checked, False otherwise
     */
    public boolean check(int position) {
        boolean checked = !checkedPositions[position];
        checkedPositions[position] = checked;
        if (checked)
            checkedCount++;
        else
            checkedCount--;
        return checked;
    }
}
