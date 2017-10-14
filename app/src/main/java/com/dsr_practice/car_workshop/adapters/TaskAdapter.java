package com.dsr_practice.car_workshop.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.viewholders.JobViewHolder;
import com.dsr_practice.car_workshop.viewholders.TaskViewHolder;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class TaskAdapter extends ExpandableRecyclerViewAdapter<TaskViewHolder, JobViewHolder> {

    public TaskAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
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
    public void onBindGroupViewHolder(TaskViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {
        Task task = ((Task) group);
        holder.setItems(task);
    }

    @Override
    public void onBindChildViewHolder(JobViewHolder holder, int flatPosition,
                                      ExpandableGroup group, int childIndex) {
        Task task = ((Task) group);
        holder.setItems(task.getJobs().get(childIndex));
    }
}
