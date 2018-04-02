package ru.usedesk.sample.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ru.usedesk.sample.R;
import ru.usedesk.sdk.UsedeskConfiguration;

public class ConfigureUsedeskDialog extends DialogFragment {

    private EditText companyIdEditText;
    private EditText emailEditText;
    private EditText urlEditText;

    private OnConfigurationUsedeskListener onConfigurationUsedeskListener;

    public ConfigureUsedeskDialog() {
    }

    public static ConfigureUsedeskDialog newInstance(OnConfigurationUsedeskListener onConfigurationSetListener) {
        ConfigureUsedeskDialog dialogFragment = new ConfigureUsedeskDialog();
        dialogFragment.onConfigurationUsedeskListener = onConfigurationSetListener;
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_configure_usedesk, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle(R.string.configuration_dialog_title);
        alertDialogBuilder.setView(view);

        companyIdEditText = (EditText) view.findViewById(R.id.company_id_edit_text);
        emailEditText = (EditText) view.findViewById(R.id.email_edit_text);
        urlEditText = (EditText) view.findViewById(R.id.url_edit_text);

        alertDialogBuilder.setPositiveButton(android.R.string.ok,  new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean companyIdEntered = !TextUtils.isEmpty(companyIdEditText.getText());
                boolean emailEntered = !TextUtils.isEmpty(emailEditText.getText());
                boolean urlEntered = !TextUtils.isEmpty(urlEditText.getText());

                if (companyIdEntered && emailEntered && urlEntered) {
                    onConfigurationUsedeskListener.onConfigurationUsedeskSet(new UsedeskConfiguration.Builder()
                            .companyId(companyIdEditText.getText().toString())
                            .email(emailEditText.getText().toString())
                            .url(urlEditText.getText().toString())
                            .build());
                }

                dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel,  new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return alertDialogBuilder.create();
    }

    public static interface OnConfigurationUsedeskListener {

        void onConfigurationUsedeskSet(UsedeskConfiguration usedeskConfiguration);
    }
}