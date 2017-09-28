package com.dsr_practice.car_workshop.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.activities.InfoActivity;
import com.dsr_practice.car_workshop.dialogs.CloseDialog;
import com.dsr_practice.car_workshop.dialogs.MessageDialog;
import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;

import java.text.DateFormat;
import java.util.List;

public class TaskListAdapter extends BaseExpandableListAdapter implements CloseDialog.CloseCallback {

    private Context context;
    private List<Task> taskList;
    private FragmentManager fragmentManager;

    private static final String CLOSE_TASK_TAG = "CLOSE_TASK_TAG";
    private static final String CLOSE_JOB_TAG  = "CLOSE_JOB_TAG";
    private static final String DIALOG_TAG = "DIALOG";

    private static String closeTaskTitle;
    private static String closeTaskMessage;

    // Icons for buttons
    private static Drawable closedTaskIcon;
    private static Drawable closedIcon;
    private static Drawable openedIcon;
    // Resource for buttons
    private static int resource;

    public TaskListAdapter(Context context, List<Task> taskList, FragmentManager fragmentManager) {
        this.context = context;
        this.taskList = taskList;
        this.fragmentManager = fragmentManager;

        closeTaskTitle = this.context.getString(R.string.task_was_closed);
        closeTaskMessage = this.context.getString(R.string.task_full_price);

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

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();
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
            convertView = inflater.inflate(R.layout.list_group, parent, false);
        }

        TextView lblCarName = (TextView) convertView.findViewById(R.id.tvCarName);
        TextView lblDate = (TextView) convertView.findViewById(R.id.tvDate);
        TextView lblNumber = (TextView) convertView.findViewById(R.id.tvNumber);

        lblCarName.setText(task.getName());
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        lblDate.setText(dateFormat.format(task.getDate()));
        lblNumber.setText(task.getNumber());

        final ImageButton imgBtnClose = (ImageButton) convertView.findViewById(R.id.imgBtnClose);

        imgBtnClose.setBackgroundResource(resource);
        // If task is closed
        if (task.getStatus())
            imgBtnClose.setImageDrawable(closedTaskIcon);
        else // task is open
            imgBtnClose.setImageDrawable(openedIcon);

        imgBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseDialog dialog = CloseDialog.newInstance(task, null,
                        R.string.close_task_title, R.string.close_task_message,
                        imgBtnClose, closedTaskIcon, TaskListAdapter.this);
                dialog.show(fragmentManager, CLOSE_TASK_TAG);
            }
        });
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final JobStatus jobStatus = (JobStatus) getChild(groupPosition, childPosition);
        final Task task = (Task) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }
        final ImageButton imgBtnCloseJob = (ImageButton) convertView.findViewById(R.id.imgBtnCloseJob);
        TextView lblWork = (TextView) convertView.findViewById(R.id.tvWork);
        TextView lblPrice = (TextView) convertView.findViewById(R.id.tvPrice);

        Job job = jobStatus.getJob();
        lblWork.setText(job.getName());
        lblPrice.setText(job.getPriceToString());

        imgBtnCloseJob.setBackgroundResource(resource);
        // If job is closed
        if (jobStatus.getStatus())
            imgBtnCloseJob.setImageDrawable(closedIcon);
        else // job is opened
            imgBtnCloseJob.setImageDrawable(openedIcon);

        imgBtnCloseJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseDialog dialog = CloseDialog.newInstance(task, jobStatus,
                        R.string.close_job_title, R.string.close_job_message,
                        imgBtnCloseJob, closedIcon, TaskListAdapter.this);
                dialog.show(fragmentManager, CLOSE_JOB_TAG);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    // Callback from CloseDialog for adapter
    @Override
    public void onJobClose(boolean isTaskClosed, Task task) {
        if (isTaskClosed)
            closeTask(task);
        notifyDataSetChanged();
    }

    public void onGroupLongClick(int groupPosition) {
        // View task info
        Task task = (Task) getGroup(groupPosition);
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(context.getString(R.string.task_intent), task);
        context.startActivity(intent);
    }

    // Dialog for closing task which shows full price of all jobs in task
    private void closeTask(Task task) {
        int price = 0;
        for (JobStatus jobStatus: task.getJobs()) {
            price += jobStatus.getJob().getPrice();
        }
        MessageDialog dialog = MessageDialog.newInstance(
                closeTaskTitle, closeTaskMessage + Integer.toString(price));
        dialog.show(fragmentManager, DIALOG_TAG);
    }
}
