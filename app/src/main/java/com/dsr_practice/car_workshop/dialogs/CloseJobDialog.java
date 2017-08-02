package com.dsr_practice.car_workshop.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.ImageButton;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;

public class CloseJobDialog extends DialogFragment implements DialogInterface.OnClickListener {
    private Task task;
    private JobStatus jobStatus;
    private ImageButton imageButton;
    private Drawable icon;
    private static CloseCallback closeCallback;

    public static CloseJobDialog newInstance(Task task, JobStatus jobStatus,
                                             ImageButton imageButton, Drawable icon,
                                             CloseCallback callback) {
        CloseJobDialog dialog = new CloseJobDialog();
        dialog.task = task;
        dialog.jobStatus = jobStatus;
        dialog.imageButton = imageButton;
        dialog.icon = icon;
        closeCallback = callback;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.close_job_title)
                .setMessage(R.string.close_job_message)
                .setPositiveButton(R.string.yes, this)
                .setNegativeButton(R.string.no, this);
        return builder.create();
    }

    @Override
    public void onDestroyView() {
        // Used because of a bug in the support library
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
            case Dialog.BUTTON_POSITIVE:
                closeTask();
                break;
        }
    }

    private void closeTask() {
        imageButton.setImageDrawable(icon);
        jobStatus.setStatus(true);
        // Check if all jobs in task are closed
        boolean allClosed = true;
        for (JobStatus jobStatus: task.getJobs()) {
            allClosed = jobStatus.getStatus();
            if (!allClosed)
                break;
        }
        if (allClosed)
            task.setStatus(true);
        closeCallback.onJobClose(allClosed, task);
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
                        if (allClosed)
                            task.setStatus(true);
                        closeCallback.onJobClose(allClosed, task);
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
}
