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

    public TaskAdapter(List<? extends ExpandableGroup> groups, Context context) {
        super(groups);
        this.context = context;
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
        Task task = ((Task) group);
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
    }

    @Override
    public void onBindChildViewHolder(JobViewHolder holder, int flatPosition,
                                      ExpandableGroup group, int childIndex) {
        Task task = ((Task) group);
        JobStatus jobStatus = task.getJobs().get(childIndex);
        holder.setItems(jobStatus);
    }
}
