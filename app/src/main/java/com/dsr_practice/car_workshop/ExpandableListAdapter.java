package com.dsr_practice.car_workshop;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;

import java.util.List;

class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Task> taskList;

    // Icons for buttons
    private static Drawable closedIcon;
    private static Drawable openedIcon;
    // Resource for buttons
    private static int resource;

    ExpandableListAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;

        // Set icons
        closedIcon = ContextCompat.getDrawable(this.context, R.drawable.ic_done_black_24dp);
        DrawableCompat.setTint(closedIcon, ContextCompat.getColor(context, android.R.color.holo_green_light));
        openedIcon = ContextCompat.getDrawable(context, R.drawable.ic_error_outline_black_24dp);
        DrawableCompat.setTint(openedIcon, ContextCompat.getColor(context, android.R.color.holo_red_dark));

        // Set resource
        TypedValue typedValue = new TypedValue();
        this.context.getTheme().resolveAttribute(R.attr.selectableItemBackground, typedValue, true);
        resource = typedValue.resourceId;
    }

    @Override
    public int getGroupCount() {
        return this.taskList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.taskList.get(groupPosition).getJobs().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.taskList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.taskList.get(groupPosition).getJobs().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
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
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final Task task = (Task) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
        }

        TextView lblCarName = (TextView) convertView.findViewById(R.id.tvCarName);
        TextView lblDate = (TextView) convertView.findViewById(R.id.tvDate);
        TextView lblNumber = (TextView) convertView.findViewById(R.id.tvNumber);

        lblCarName.setText(task.getName());
        lblDate.setText(task.getDate().toString());
        lblNumber.setText(task.getNumber());

        ImageButton imgBtnClose = (ImageButton) convertView.findViewById(R.id.imgBtnClose);

        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final JobStatus jobStatus = (JobStatus) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }
        ImageButton imgBtnCloseJob = (ImageButton) convertView.findViewById(R.id.imgBtnCloseJob);
        TextView lblWork = (TextView) convertView.findViewById(R.id.tvWork);
        TextView lblPrice = (TextView) convertView.findViewById(R.id.tvPrice);

        // Get job by id in job status!!!!
        lblWork.setText("Car wash");
        lblPrice.setText("300 RUB");

        // If job is closed
        if (jobStatus.getStatus()) {
            imgBtnCloseJob.setBackgroundResource(resource);
            imgBtnCloseJob.setImageDrawable(closedIcon);

        }
        else {
            // job is opened
            imgBtnCloseJob.setBackgroundResource(resource);
            imgBtnCloseJob.setImageDrawable(openedIcon);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
