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

public class CloseTaskDialog extends DialogFragment implements DialogInterface.OnClickListener {
    private Task task;
    private ImageButton imageButton;
    private Drawable icon;

    public static CloseTaskDialog newInstance(Task task, ImageButton imageButton, Drawable icon) {
        CloseTaskDialog dialog = new CloseTaskDialog();
        dialog.task = task;
        dialog.imageButton = imageButton;
        dialog.icon = icon;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.close_task_title)
                .setMessage(R.string.close_task_message)
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
                closeJob();
                break;
        }
    }

    private void closeJob() {
        imageButton.setImageDrawable(icon);
        task.setStatus(true);
        for (JobStatus jobStatus: task.getJobs()) {
            jobStatus.setStatus(true);
        }
        //TODO Callback to adapter
        /*
        apiInterface.closeTask(task.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                imageButton.setImageDrawable(icon);
                task.setStatus(true);
                for (JobStatus jobStatus: task.getJobs()) {
                    jobStatus.setStatus(true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, R.string.toast_cant_close_task, Toast.LENGTH_SHORT).show();
            }
        });*/
    }
}
