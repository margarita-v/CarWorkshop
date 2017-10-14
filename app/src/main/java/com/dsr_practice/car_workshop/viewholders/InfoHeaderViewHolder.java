package com.dsr_practice.car_workshop.viewholders;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.Task;

public class InfoHeaderViewHolder extends ViewHolder {

    private TextView tvStatus;
    private TextView tvVin;
    private TextView tvMark;
    private TextView tvModel;
    private TextView tvDate;
    private TextView tvNumber;

    private static final String TASK_CLOSED = "Закрыта";
    private static final String TASK_OPENED = "Открыта";

    public InfoHeaderViewHolder(View itemView) {
        super(itemView);
        this.tvStatus = itemView.findViewById(R.id.tvStatus);
        this.tvVin = itemView.findViewById(R.id.tvVIN);
        this.tvMark = itemView.findViewById(R.id.tvMark);
        this.tvModel = itemView.findViewById(R.id.tvModel);
        this.tvDate = itemView.findViewById(R.id.tvDate);
        this.tvNumber = itemView.findViewById(R.id.tvNumber);
    }

    public void setItems(Task task, String mark, String model) {
        this.tvStatus.setText(task.getStatus() ? TASK_CLOSED : TASK_OPENED);
        this.tvVin.setText(task.getVin());
        this.tvMark.setText(mark);
        this.tvModel.setText(model);
        this.tvDate.setText(task.getDateToString());
        this.tvNumber.setText(task.getNumber());
    }
}
