package com.dsr_practice.car_workshop.viewholders;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.sync.Job;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

public class JobViewHolder extends ChildViewHolder {

    private ImageButton imgBtnCloseJob;
    private TextView tvJob;
    private TextView tvPrice;

    public JobViewHolder(View itemView) {
        super(itemView);
        this.imgBtnCloseJob = itemView.findViewById(R.id.imgBtnCloseJob);
        this.tvJob = itemView.findViewById(R.id.tvWork);
        this.tvPrice = itemView.findViewById(R.id.tvPrice);
    }

    public void setItems(JobStatus jobStatus, Drawable icon) {
        Job job = jobStatus.getJob();
        this.tvJob.setText(job.getName());
        this.tvPrice.setText(job.getPriceToString());
        this.imgBtnCloseJob.setImageDrawable(icon);
    }

    public ImageButton getBtnCloseJob() {
        return this.imgBtnCloseJob;
    }
}
