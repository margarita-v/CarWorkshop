package com.dsr_practice.car_workshop.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;

import java.text.DateFormat;
import java.util.List;

public class TaskListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Task> taskList;

    // Icons for buttons
    private static Drawable closedTaskIcon;
    private static Drawable closedIcon;
    private static Drawable openedIcon;
    // Resource for buttons
    private static int resource;

    public TaskListAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;

        // Set icons
        closedTaskIcon = IconsUtils.getIcon(
                this.context, R.drawable.ic_done_all_black_24dp, android.R.color.holo_green_light);
        closedIcon = IconsUtils.getIcon(
                this.context, R.drawable.ic_done_black_24dp, android.R.color.holo_green_light);
        openedIcon = IconsUtils.getIcon(
                this.context, R.drawable.ic_error_outline_black_24dp, android.R.color.holo_red_dark);

        // Set resource
        resource = IconsUtils.getResource(this.context);
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
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        lblDate.setText(dateFormat.format(task.getDate()));
        lblNumber.setText(task.getNumber());

        final ImageButton imgBtnClose = (ImageButton) convertView.findViewById(R.id.imgBtnClose);
        if (imgBtnClose.isClickable()) {
            imgBtnClose.setBackgroundResource(resource);
            // If task is closed
            if (task.getStatus())
                close(imgBtnClose, closedTaskIcon);
            else // task is open
                imgBtnClose.setImageDrawable(openedIcon);
        }

        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Close task").setMessage("Are you sure to close this task?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        close(imgBtnClose, closedTaskIcon);
                        //TODO Send POST request to server
                        task.setStatus(true);
                        //TODO Close all jobs in this task
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
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
        final ImageButton imgBtnCloseJob = (ImageButton) convertView.findViewById(R.id.imgBtnCloseJob);
        TextView lblWork = (TextView) convertView.findViewById(R.id.tvWork);
        TextView lblPrice = (TextView) convertView.findViewById(R.id.tvPrice);

        // Get job by id in job status!!!!
        lblWork.setText("Car wash");
        lblPrice.setText("300 RUB");

        if (imgBtnCloseJob.isClickable()) {
            imgBtnCloseJob.setBackgroundResource(resource);
            // If job is closed
            if (jobStatus.getStatus())
                close(imgBtnCloseJob, closedIcon);
            else // job is opened
                imgBtnCloseJob.setImageDrawable(openedIcon);
        }

        imgBtnCloseJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Close job").setMessage("Are you sure to close this job?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        close(imgBtnCloseJob, closedIcon);
                        //TODO Send POST request to server
                        jobStatus.setStatus(true);
                        //TODO Check if all jobs in task are closed
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void close(ImageButton imageButton, Drawable drawable) {
        imageButton.setImageDrawable(drawable);
        imageButton.setClickable(false);
    }
}
