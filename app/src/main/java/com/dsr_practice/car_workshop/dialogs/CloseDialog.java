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
import com.dsr_practice.car_workshop.models.common.Task;

public class CloseDialog extends DialogFragment {

    // Keys for bundle
    private static final String TITLE_KEY = "TITLE_KEY";
    private static final String MESSAGE_KEY = "MESSAGE_KEY";
    private static final String TASK_KEY = "TASK_KEY";
    private static final String JOB_KEY = "JOB_KEY";

    // Callback to activity
    private CloseInterface closeActionListener;

    // Tag for dialog usage
    public static final String TAG = "CLOSE_DIALOG";

    public static CloseDialog newInstance(int titleId, int messageId,
                                          Task task, Integer jobId) {
        Bundle args = new Bundle();
        args.putInt(TITLE_KEY, titleId);
        args.putInt(MESSAGE_KEY, messageId);
        args.putParcelable(TASK_KEY, task);
        if (jobId != null)
            args.putInt(JOB_KEY, jobId);

        CloseDialog dialog = new CloseDialog();
        dialog.setArguments(args);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Bundle args = getArguments();
        if (args != null) {
            final Task task = args.getParcelable(TASK_KEY);
            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case Dialog.BUTTON_NEGATIVE:
                            dismiss();
                            break;
                        case Dialog.BUTTON_POSITIVE:
                            if (args.containsKey(JOB_KEY))
                                closeActionListener.onJobClose(args.getInt(JOB_KEY), task);
                            else
                                closeActionListener.onTaskClose(task);
                    }
                }
            };
            builder.setTitle(args.getInt(TITLE_KEY))
                .setMessage(args.getInt(MESSAGE_KEY))
                .setPositiveButton(R.string.yes, onClickListener)
                .setNegativeButton(R.string.no, onClickListener);
        }
        return builder.create();
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
         * @param jobId ID of job in concrete task which will be closed
         * @param task Task which associated with closing job
         */
        void onJobClose(Integer jobId, Task task);
    }
}
