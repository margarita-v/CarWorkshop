package com.dsr_practice.car_workshop.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.viewholders.InfoHeaderViewHolder;
import com.dsr_practice.car_workshop.viewholders.JobViewHolder;

public class TaskInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Task task;
    private String mark;
    private String model;

    private static final int JOB_ID = 0, HEADER_ID = 1;

    // Icons for buttons
    private static Drawable closedIcon;
    private static Drawable openedIcon;

    public TaskInfoAdapter(Task task, String mark, String model, Context context) {
        this.task = task;
        this.mark = mark;
        this.model = model;

        // Set icons
        closedIcon = IconsUtils.getIcon(
                context, R.drawable.ic_done_black_24dp, R.color.colorClosed);
        openedIcon = IconsUtils.getIcon(
                context, R.drawable.ic_error_outline_black_24dp, R.color.colorOpened);
    }

    @Override
    public int getItemCount() {
        return this.task.getJobs().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position != 0)
            return JOB_ID;
        return HEADER_ID;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case JOB_ID:
                view = inflater.inflate(R.layout.list_item, parent, false);
                viewHolder = new JobViewHolder(view);
                break;
            case HEADER_ID:
                view = inflater.inflate(R.layout.info_header, parent, false);
                viewHolder = new InfoHeaderViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position != 0)
            configureJobViewHolder((JobViewHolder) holder, position - 1);
        else
            configureHeaderViewHolder((InfoHeaderViewHolder) holder);

    }

    //region Configure different view holders
    private void configureHeaderViewHolder(InfoHeaderViewHolder holder) {
        holder.setItems(this.task, this.mark, this.model);
    }

    private void configureJobViewHolder(JobViewHolder holder, int position) {
        holder.getBtnCloseJob().setEnabled(false);
        JobStatus jobStatus = this.task.getJobs().get(position);
        holder.setItems(jobStatus, jobStatus.getStatus() ? closedIcon : openedIcon);
    }
    //endregion
}
