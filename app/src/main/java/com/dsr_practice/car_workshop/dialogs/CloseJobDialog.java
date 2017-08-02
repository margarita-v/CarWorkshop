package com.dsr_practice.car_workshop.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageButton;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;

public class CloseJobDialog extends DialogFragment {
    private Task task;
    private JobStatus jobStatus;
    private ImageButton imageButton;
    private Drawable icon;
    private DialogInterface.OnClickListener onClickListener;

    public static CloseJobDialog newInstance(Task task, JobStatus jobStatus,
                                             ImageButton imageButton, Drawable icon,
                                             DialogInterface.OnClickListener onClickListener) {
        CloseJobDialog dialog = new CloseJobDialog();
        dialog.task = task;
        dialog.jobStatus = jobStatus;
        dialog.imageButton = imageButton;
        dialog.icon = icon;
        dialog.onClickListener = onClickListener;
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.close_job_title);
        builder.setMessage(R.string.close_job_message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                imageButton.setImageDrawable(icon);
                jobStatus.setStatus(true);
                // Check if all jobs in task are closed
                boolean allClosed = true;
                for (JobStatus jobStatus: task.getJobs()) {
                    allClosed = jobStatus.getStatus();
                    if (!allClosed)
                        break;
                }
                if (allClosed) {
                    task.setStatus(true);
                    //TODO Callback to adapter
                    //closeTask(task);
                }
                //notifyDataSetChanged();
                /*
                apiInterface.closeJobInTask(new CloseJobPost(jobStatus.getId(), task.getId()))
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                imgBtnCloseJob.setImageDrawable(closedIcon);
                                jobStatus.setStatus(true);
                                boolean allClosed = true;
                                for (JobStatus jobStatus: task.getJobs()) {
                                    allClosed = jobStatus.getStatus();
                                    if (!allClosed)
                                        break;
                                }
                                if (allClosed) {
                                    task.setStatus(true);
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(
                                        context,
                                        R.string.toast_cant_close_job,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });*/
            }
        });
        builder.setNegativeButton(R.string.no, onClickListener);
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        // Used because of a bug in the support library
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
}
