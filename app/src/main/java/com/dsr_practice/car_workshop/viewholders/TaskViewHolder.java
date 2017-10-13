package com.dsr_practice.car_workshop.viewholders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.Task;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.text.DateFormat;

public class TaskViewHolder extends GroupViewHolder {

    private TextView tvCarName;
    private TextView tvDate;
    private TextView tvNumber;
    private ImageButton imgBtnCloseTask;
    private DateFormat dateFormat;

    public TaskViewHolder(View itemView) {
        super(itemView);
        this.tvCarName = itemView.findViewById(R.id.tvCarName);
        this.tvDate = itemView.findViewById(R.id.tvDate);
        this.tvNumber = itemView.findViewById(R.id.tvNumber);
        this.imgBtnCloseTask = itemView.findViewById(R.id.imgBtnClose);
        this.dateFormat = DateFormat.getDateTimeInstance();
    }

    public void setItems(Task task) {
        this.tvCarName.setText(task.getName());
        this.tvDate.setText(dateFormat.format(task.getDate()));
        this.tvNumber.setText(task.getNumber());
    }

    public ImageButton getBtnCloseTask() {
        return this.imgBtnCloseTask;
    }
}
