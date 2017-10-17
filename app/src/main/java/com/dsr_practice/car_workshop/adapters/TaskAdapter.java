package com.dsr_practice.car_workshop.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.activities.InfoActivity;
import com.dsr_practice.car_workshop.dialogs.CloseDialog;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.viewholders.JobViewHolder;
import com.dsr_practice.car_workshop.viewholders.TaskViewHolder;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class TaskAdapter extends ExpandableRecyclerViewAdapter<TaskViewHolder, JobViewHolder> {

    private Context context;
    private FragmentManager fragmentManager;

    // Tag for dialog usage
    private static final String DIALOG_TAG = "DIALOG";

    public TaskAdapter(List<? extends ExpandableGroup> groups,
                       Context context,
                       FragmentManager fragmentManager) {
        super(groups);
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public TaskViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_group, parent, false));
    }

    @Override
    public JobViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        return new JobViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindGroupViewHolder(TaskViewHolder holder, final int flatPosition,
                                      final ExpandableGroup group) {
        final Task task = ((Task) group);
        holder.setItems(task);
        holder.getBtnTaskInfo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task task = (Task) group;
                Intent intent = new Intent(context, InfoActivity.class);
                intent.putExtra(context.getString(R.string.task_intent), task);
                context.startActivity(intent);
            }
        });
        holder.getBtnCloseTask().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                configureConfirmDialog(R.string.close_task_title, R.string.close_task_message,
                        task, null);
            }
        });
    }

    @Override
    public void onBindChildViewHolder(JobViewHolder holder, int flatPosition,
                                      ExpandableGroup group, int childIndex) {
        final Task task = ((Task) group);
        final JobStatus jobStatus = task.getJobs().get(childIndex);
        holder.setItems(jobStatus);
        holder.getBtnCloseJob().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                configureConfirmDialog(R.string.close_job_title, R.string.close_job_message,
                        task, jobStatus);
            }
        });
    }

    /**
     * Configure dialog for close action confirmation
     * @param titleId ID of title' string resource
     * @param messageId ID of message's string resource
     * @param task Task which will be closed
     * @param jobStatus Job which will be closed
     */
    private void configureConfirmDialog(int titleId, int messageId,
                                        Task task, JobStatus jobStatus) {
        CloseDialog.newInstance(titleId, messageId, task, jobStatus)
                .show(fragmentManager, DIALOG_TAG);
    }
}
