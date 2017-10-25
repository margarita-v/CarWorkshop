package com.dsr_practice.car_workshop.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.Task;

public class TaskViewHolder extends RecyclerView.ViewHolder implements IconInterface {

    private TextView tvCarName;
    private TextView tvDate;
    private TextView tvNumber;
    private ImageButton imgBtnCloseTask;
    private ImageButton imgBtnInfo;

    public TaskViewHolder(View itemView) {
        super(itemView);
        this.tvCarName = itemView.findViewById(R.id.tvCarName);
        this.tvDate = itemView.findViewById(R.id.tvDate);
        this.tvNumber = itemView.findViewById(R.id.tvNumber);
        this.imgBtnCloseTask = itemView.findViewById(R.id.imgBtnClose);
        this.imgBtnInfo = itemView.findViewById(R.id.imgBtnInfo);
    }

    public void setItems(Task task) {
        this.tvCarName.setText(task.getName());
        this.tvDate.setText(task.getDateToString());
        this.tvNumber.setText(task.getNumber());
        this.imgBtnCloseTask.setEnabled(!task.getStatus());
        this.imgBtnCloseTask.setImageResource(task.getStatus()
                ? TASK_CLOSED_IMAGE_ID : ITEM_OPENED_IMAGE_ID);
    }

    public ImageButton getBtnCloseTask() {
        return this.imgBtnCloseTask;
    }

    public ImageButton getBtnTaskInfo() {
        return this.imgBtnInfo;
    }
}
