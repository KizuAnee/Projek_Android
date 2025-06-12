package com.example.myapplication.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ResultDialog extends DialogFragment {

    private String message;
    private String buttonText;
    private Runnable onButtonClick;

    public ResultDialog(String message, String buttonText, Runnable onButtonClick) {
        this.message = message;
        this.buttonText = buttonText;
        this.onButtonClick = onButtonClick;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(message)
                .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (onButtonClick != null) {
                            onButtonClick.run();
                        }
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}