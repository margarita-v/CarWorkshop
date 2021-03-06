package com.dsr_practice.car_workshop.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.activities.InfoActivity;
import com.dsr_practice.car_workshop.dialogs.CloseDialog;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.viewholders.JobViewHolder;
import com.dsr_practice.car_workshop.viewholders.TaskViewHolder;

import java.util.List;

public class TaskAdapter extends BaseExpandableListAdapter {

    private SparseArray<Task> taskSparseArray;
    private Context context;
    private FragmentManager fragmentManager;

    public TaskAdapter(List<Task> taskList,
                       Context context, FragmentManager fragmentManager) {

        this.context = context;
        this.fragmentManager = fragmentManager;
        this.taskSparseArray = new SparseArray<>();
        for (Task task: taskList) {
            this.taskSparseArray.put(task.getId(), task);
        }
    }

    @Override
    public int getGroupCount() {
        return this.taskSparseArray.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return getGroup(i).getJobs().size();
    }

    @Override
    public Task getGroup(int i) {
        return this.taskSparseArray.valueAt(i);
    }

    @Override
    public JobStatus getChild(int i, int i1) {
        return getGroup(i).getJobs().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return getGroup(i).getId();
    }

    @Override
    public long getChildId(int i, int i1) {
        return getChild(i, i1).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

        final Task task = getGroup(i);
        TaskViewHolder viewHolder;

        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_group, viewGroup, false);
            viewHolder = new TaskViewHolder(view);
            view.setTag(viewHolder);
        }
        else
            viewHolder = (TaskViewHolder) view.getTag();

        viewHolder.setItems(task);
        viewHolder.getBtnTaskInfo().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, InfoActivity.class);
                intent.putExtra(context.getString(R.string.task_intent), task);
                context.startActivity(intent);
            }
        });
        viewHolder.getBtnCloseTask().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmDialog(R.string.close_task_title, R.string.close_task_message,
                        task, null);
            }
        });
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {

        final JobStatus jobStatus = getChild(i, i1);
        final Task task = getGroup(i);
        JobViewHolder viewHolder;

        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_item, viewGroup, false);
            viewHolder = new JobViewHolder(view);
            view.setTag(viewHolder);
        }
        else
            viewHolder = (JobViewHolder) view.getTag();

        viewHolder.setItems(jobStatus);
        viewHolder.getBtnCloseJob().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmDialog(R.string.close_job_title, R.string.close_job_message,
                        task, jobStatus.getJob().getId());
            }
        });
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    /**
     * Change task by id after closing action
     * @param task Response from server
     */
    public void setTask(Task task) {
        this.taskSparseArray.put(task.getId(), task);
        notifyDataSetChanged();
    }

    /**
     * Show dialog for close action confirmation
     * @param titleId ID of title' string resource
     * @param messageId ID of message's string resource
     * @param task Task which will be closed
     * @param jobId ID of job which will be closed
     */
    private void showConfirmDialog(int titleId, int messageId,
                                   Task task, Integer jobId) {
        CloseDialog.newInstance(titleId, messageId, task, jobId)
                .show(fragmentManager, CloseDialog.TAG);
    }
}
