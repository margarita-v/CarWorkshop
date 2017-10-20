package com.dsr_practice.car_workshop.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.dsr_practice.car_workshop.R;
import com.dsr_practice.car_workshop.models.common.JobStatus;
import com.dsr_practice.car_workshop.models.common.Task;

public class CloseDialog extends DialogFragment implements DialogInterface.OnClickListener {

    // IDs of resources for dialog's title and message
    private int titleId, messageId;

    // Dialog's objects
    private Task task;
    private JobStatus jobStatus;

    // Callback to activity
    private CloseInterface closeActionListener;

    // Tag for dialog usage
    public static final String TAG = "CLOSE_DIALOG";

    public static CloseDialog newInstance(int titleId, int messageId,
                                          Task task, JobStatus jobStatus) {
        CloseDialog dialog = new CloseDialog();
        dialog.task = task;
        dialog.jobStatus = jobStatus;
        dialog.titleId = titleId;
        dialog.messageId = messageId;
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        try {
            closeActionListener = (CloseInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + R.string.class_cast);
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.yes, this)
                .setNegativeButton(R.string.no, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_NEGATIVE:
                dismiss();
                break;
            case Dialog.BUTTON_POSITIVE:
                if (jobStatus != null)
                    closeActionListener.onJobClose(jobStatus, task);
                else
                    closeActionListener.onTaskClose(task);
        }
    }

    /**
     * Interface for task and job closing
     */
    public interface CloseInterface {

        /**
         * Close task action
         * @param task Task which will be closed
         */
        void onTaskClose(Task task);

        /**
         * Close job action
         * @param jobStatus JobStatus of job in concrete task which will be closed
         * @param task Task which associated with closing job
         */
        void onJobClose(JobStatus jobStatus, Task task);
    }
}
