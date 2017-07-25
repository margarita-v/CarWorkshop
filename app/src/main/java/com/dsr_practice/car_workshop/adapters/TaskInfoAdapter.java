package com.dsr_practice.car_workshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.models.common.Task;

import java.text.DateFormat;
import java.util.List;

public class TaskInfoAdapter extends BaseExpandableListAdapter {

    private Context context;
    private Task task;
    private List<Job> jobs;

    public TaskInfoAdapter(Context context, Task task) {
        this.context = context;
        this.task = task;
        //TODO Get jobs (query to DB)
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.task.getJobs().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.task;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.task.getJobs().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 1;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.info_group, null);
        }

        TextView tvVin = (TextView) convertView.findViewById(R.id.tvVIN);
        TextView tvMark = (TextView) convertView.findViewById(R.id.tvMark);
        TextView tvModel = (TextView) convertView.findViewById(R.id.tvModel);
        TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);
        TextView tvNumber = (TextView) convertView.findViewById(R.id.tvNumber);

        tvVin.setText(this.task.getVin());
        tvMark.setText(this.task.getMark());
        tvModel.setText(this.task.getModel());
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        tvDate.setText(dateFormat.format(task.getDate()));
        tvNumber.setText(this.task.getNumber());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }

        ImageButton imgBtnJobStatus = (ImageButton) convertView.findViewById(R.id.imgBtnCloseJob);
        TextView lblWork = (TextView) convertView.findViewById(R.id.tvWork);
        TextView lblPrice = (TextView) convertView.findViewById(R.id.tvPrice);

        imgBtnJobStatus.setClickable(false);

        // Set text and icons
        return null;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
