package com.dsr_practice.car_workshop.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;

import java.util.HashMap;

public class TaskInfoAdapter extends ArrayAdapter<JobStatus> implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context context;
    private HashMap<Integer, Job> jobs;

    private static final String[] JOB_PROJECTION = new String[] {
            Contract.JobEntry.COLUMN_NAME_JOB_NAME,
            Contract.JobEntry.COLUMN_NAME_PRICE
    };

    // Icons for buttons
    private static Drawable closedIcon;
    private static Drawable openedIcon;
    // Resource for buttons
    private static int resource;

    public TaskInfoAdapter(Context context, Task task) {
        super(context, -1, task.getJobs());
        this.context = context;

        // Set icons
        closedIcon = IconsUtils.getIcon(
                this.context, R.drawable.ic_done_black_24dp, android.R.color.holo_green_light);
        openedIcon = IconsUtils.getIcon(
                this.context, R.drawable.ic_error_outline_black_24dp, android.R.color.holo_red_dark);

        // Set resource
        resource = IconsUtils.getResource(this.context);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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

        viewHolder.imgBtn.setEnabled(false);
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
