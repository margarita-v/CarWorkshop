package com.dsr_practice.car_workshop.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class MessageDialog extends DialogFragment implements DialogInterface.OnClickListener {
    private static final int PARAMS_ID = 1;
    private static final int PARAMS_STRING = 2;

    private int dialogId;
    private int titleId;
    private int messageId;

    private String title;
    private String message;

    public static MessageDialog newInstance(int titleId, int messageId) {
        MessageDialog dialog = new MessageDialog();
        dialog.titleId = titleId;
        dialog.messageId = messageId;
        dialog.dialogId = PARAMS_ID;
        return dialog;
    }

    public static MessageDialog newInstance(String title, String message) {
        MessageDialog dialog = new MessageDialog();
        dialog.title = title;
        dialog.message = message;
        dialog.dialogId = PARAMS_STRING;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setPositiveButton(android.R.string.ok, this);
        switch (dialogId) {
            case PARAMS_ID:
                builder.setTitle(titleId).setMessage(messageId);
                break;
            case PARAMS_STRING:
                builder.setTitle(title).setMessage(message);
                break;
        }
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dismiss();
    }

    @Override
    public void onDestroyView() {
        // Used because of a bug in the support library
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
}
