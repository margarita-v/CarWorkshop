package com.dsr_practice.car_workshop.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class MessageDialog extends DialogFragment implements DialogInterface.OnClickListener {

    // IDs for dialog's arguments types
    private static final int PARAMS_ID = 1;
    private static final int PARAMS_STRING = 2;

    // Keys for bundle
    private static final String DIALOG_KEY = "DIALOG_KEY";
    private static final String TITLE_KEY = "TITLE_KEY";
    private static final String MESSAGE_KEY = "MESSAGE_KEY";

    // Tag for dialog usage
    public static final String TAG = "MESSAGE_DIALOG";

    // Constructor for title and message as string resources
    public static MessageDialog newInstance(int titleId, int messageId) {
        Bundle args = new Bundle();
        args.putInt(TITLE_KEY, titleId);
        args.putInt(MESSAGE_KEY, messageId);
        args.putInt(DIALOG_KEY, PARAMS_ID);
        return createDialog(args);
    }

    // Constructor for title and message as Strings
    public static MessageDialog newInstance(String title, String message) {
        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(MESSAGE_KEY, message);
        args.putInt(DIALOG_KEY, PARAMS_STRING);
        return createDialog(args);
    }

    /**
     * Create dialog and set its arguments
     * @param args Dialog's arguments
     * @return MessageDialog with saved arguments in bundle
     */
    private static MessageDialog createDialog(Bundle args) {
        MessageDialog dialog = new MessageDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(android.R.string.ok, this);

        Bundle args = getArguments();
        if (args != null) {
            int dialogId = args.getInt(DIALOG_KEY);
            switch (dialogId) {
                case PARAMS_ID:
                    int titleId = args.getInt(TITLE_KEY);
                    int messageId = args.getInt(MESSAGE_KEY);
                    builder.setTitle(titleId).setMessage(messageId);
                    break;
                case PARAMS_STRING:
                    String title = args.getString(TITLE_KEY);
                    String message = args.getString(MESSAGE_KEY);
                    builder.setTitle(title).setMessage(message);
                    break;
            }
        }
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dismiss();
    }
}
