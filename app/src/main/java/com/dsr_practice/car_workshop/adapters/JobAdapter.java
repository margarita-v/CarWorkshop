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

public class JobAdapter extends SimpleCursorAdapter {
    private Context context;
    private Cursor cursor;
    private boolean[] checkedPositions;

    public JobAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
        this.cursor = c;
        int count = this.cursor != null ? this.cursor.getCount() : 0;
        this.checkedPositions = new boolean[count];
    }

    public JobAdapter(boolean[] checkedPositions,
                      Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
        this.cursor = c;
        this.checkedPositions = checkedPositions;
    }

    @Override
    public Cursor swapCursor(Cursor cursor) {
        this.checkedPositions = new boolean[cursor.getCount()];
        return super.swapCursor(cursor);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.job_item, null);
        }
        CheckBox cbName = (CheckBox) convertView.findViewById(R.id.cbName);
        cbName.setChecked(checkedPositions[position]);
        return super.getView(position, convertView, parent);
    }

    public int getCheckedCount() {
        int result = 0;
        for (boolean checkedPosition : checkedPositions)
            if (checkedPosition)
                result++;
        return result;
    }

    public boolean[] getCheckedPositions() {
        return checkedPositions;
    }

    public void setCheckedPositions(boolean[] checkedPositions) {
        this.checkedPositions = checkedPositions;
    }

    public void check(int position) {
        boolean checked = !checkedPositions[position];
        checkedPositions[position] = checked;
    }
}
