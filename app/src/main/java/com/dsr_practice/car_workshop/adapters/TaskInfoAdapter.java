package com.dsr_practice.car_workshop.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class TaskInfoAdapter extends ArrayAdapter<JobStatus> {

    private Context context;
    private Task task;
    private List<Job> jobs;

    // Icons for buttons
    private static Drawable closedIcon;
    private static Drawable openedIcon;
    // Resource for buttons
    private static int resource;

    public TaskInfoAdapter(Context context, Task task) {
        super(context, -1, task.getJobs());
        this.context = context;
        this.task = task;
        //TODO Get jobs (query to DB)

        // Set icons
        closedIcon = IconsUtils.getIcon(
                this.context, R.drawable.ic_done_black_24dp, android.R.color.holo_green_light);
        openedIcon = IconsUtils.getIcon(
                this.context, R.drawable.ic_error_outline_black_24dp, android.R.color.holo_red_dark);

        // Set resource
        resource = IconsUtils.getResource(this.context);
    }

    private static class ViewHolder {
        ImageButton imgBtn;
        TextView tvJob;
        TextView tvPrice;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final JobStatus jobStatus = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_item, parent, false);
            viewHolder.imgBtn = (ImageButton) convertView.findViewById(R.id.imgBtnCloseJob);
            viewHolder.tvJob = (TextView) convertView.findViewById(R.id.tvWork);
            viewHolder.tvPrice = (TextView) convertView.findViewById(R.id.tvPrice);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.tvJob.setText("Job");
        viewHolder.tvPrice.setText("300 RUB");

        viewHolder.imgBtn.setBackgroundResource(resource);
        // If job is closed
        if (jobStatus.getStatus())
            viewHolder.imgBtn.setImageDrawable(closedIcon);
        else // job is opened
            viewHolder.imgBtn.setImageDrawable(openedIcon);
        return convertView;
    }
}
