package com.dsr_practice.car_workshop.viewholders;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.Task;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.text.DateFormat;
import java.util.Locale;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class TaskViewHolder extends GroupViewHolder {

    private ImageView imgArrow;
    private TextView tvCarName;
    private TextView tvDate;
    private TextView tvNumber;
    private ImageButton imgBtnCloseTask;
    private ImageButton imgBtnInfo;
    private DateFormat dateFormat;

    public TaskViewHolder(View itemView) {
        super(itemView);
        this.imgArrow = itemView.findViewById(R.id.imgArrow);
        this.tvCarName = itemView.findViewById(R.id.tvCarName);
        this.tvDate = itemView.findViewById(R.id.tvDate);
        this.tvNumber = itemView.findViewById(R.id.tvNumber);
        this.imgBtnCloseTask = itemView.findViewById(R.id.imgBtnClose);
        this.imgBtnInfo = itemView.findViewById(R.id.imgBtnInfo);
        this.dateFormat = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
    }

    public void setItems(Task task, Drawable icon) {
        this.tvCarName.setText(task.getName());
        this.tvDate.setText(dateFormat.format(task.getDate()));
        this.tvNumber.setText(task.getNumber());
        this.imgBtnCloseTask.setImageDrawable(icon);
    }

    public ImageButton getBtnCloseTask() {
        return this.imgBtnCloseTask;
    }

    public ImageButton getBtnTaskInfo() {
        return this.imgBtnInfo;
    }

    @Override
    public void expand() {
        animateExpand();
    }

    @Override
    public void collapse() {
        animateCollapse();
    }

    private void animateExpand() {
        RotateAnimation rotate =
                new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        imgArrow.setAnimation(rotate);
    }

    private void animateCollapse() {
        RotateAnimation rotate =
                new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        imgArrow.setAnimation(rotate);
    }
}
