package com.dsr_practice.car_workshop.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.activities.InfoActivity;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.viewholders.JobViewHolder;
import com.dsr_practice.car_workshop.viewholders.TaskViewHolder;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class TaskAdapter extends ExpandableRecyclerViewAdapter<TaskViewHolder, JobViewHolder> {

    private Context context;
    private CloseInterface closeListener;

    public TaskAdapter(List<? extends ExpandableGroup> groups,
                       Context context,
                       CloseInterface closeListener) {
        super(groups);
        this.context = context;
        this.closeListener = closeListener;
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
                                      ExpandableGroup group) {
        final Task task = ((Task) group);
        holder.setItems(task);
        holder.getBtnTaskInfo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task task = (Task) getGroups().get(flatPosition);
                Intent intent = new Intent(context, InfoActivity.class);
                intent.putExtra(context.getString(R.string.task_intent), task);
                context.startActivity(intent);
            }
        });
        holder.getBtnCloseTask().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeListener.onTaskClose(task);
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
                if (closeListener.onJobClose(jobStatus, task))
                    notifyDataSetChanged();
            }
        });
    }

    /**
     * Interface for task and job closing
     */
    public interface CloseInterface {

        /**
         * Close task action
         * @param task Task which will be closed
         */
        void onTaskClose(Task task);

        /**
         * Close job action
         * @param jobStatus JobStatus of job in concrete task which will be closed
         * @param task Task which associated with closing job
         * @return True if task was closed
         */
        boolean onJobClose(JobStatus jobStatus, Task task);
    }
}
