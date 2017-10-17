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

public class MessageDialog extends DialogFragment implements DialogInterface.OnClickListener {

    // IDs for dialog's arguments types
    private static final int PARAMS_ID = 1;
    private static final int PARAMS_STRING = 2;

    // Keys for bundle
    private static final String DIALOG_KEY = "DIALOG_KEY";
    private static final String TITLE_KEY = "TITLE_KEY";
    private static final String MESSAGE_KEY = "MESSAGE_KEY";
    private static final String CLOSE_QUESTION = "CLOSE_QUESTION";

    private ConfirmClose confirmCloseListener;

    // Constructor for title and message as string resources
    public static MessageDialog newInstance(int titleId, int messageId, boolean useForClose) {
        Bundle args = new Bundle();
        args.putInt(TITLE_KEY, titleId);
        args.putInt(MESSAGE_KEY, messageId);
        args.putInt(DIALOG_KEY, PARAMS_ID);
        args.putBoolean(CLOSE_QUESTION, useForClose);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        try {
            confirmCloseListener = (ConfirmClose) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + R.string.class_cast);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle args = getArguments();
        if (args != null) {
            int dialogId = args.getInt(DIALOG_KEY);
            switch (dialogId) {
                case PARAMS_ID:
                    int titleId = args.getInt(TITLE_KEY);
                    int messageId = args.getInt(MESSAGE_KEY);
                    boolean useForClose = args.getBoolean(CLOSE_QUESTION);
                    builder.setTitle(titleId).setMessage(messageId);
                    // If dialog is used for close action
                    if (useForClose) {
                        builder.setTitle(titleId)
                                .setMessage(messageId)
                                .setNegativeButton(R.string.no, this);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                confirmCloseListener.onCloseAction();
                            }
                        });
                    }
                    else
                        // If dialog is used for message only
                        setDismissButton(builder);
                    break;
                case PARAMS_STRING:
                    String title = args.getString(TITLE_KEY);
                    String message = args.getString(MESSAGE_KEY);
                    setDismissButton(builder);
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

    private void setDismissButton(AlertDialog.Builder builder) {
        builder.setPositiveButton(android.R.string.ok, this);
    }

    /**
     * Interface for confirm close action
     */
    public interface ConfirmClose {

        /**
         * Function will called when user will choose positive button to perform close action
         */
        void onCloseAction();
    }
}
