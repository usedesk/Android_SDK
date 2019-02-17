package ru.usedesk.sample.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ru.usedesk.sample.R;
import ru.usedesk.sdk.domain.entity.chat.UsedeskConfiguration;

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
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_configure_usedesk, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle(R.string.configuration_dialog_title);
        alertDialogBuilder.setView(view);

        companyIdEditText = view.findViewById(R.id.company_id_edit_text);
        emailEditText = view.findViewById(R.id.email_edit_text);
        urlEditText = view.findViewById(R.id.url_edit_text);

        //TODO:DEBUG
        companyIdEditText.setText("153712");
        emailEditText.setText("example@m.c");
        urlEditText.setText("https://pubsub.usedesk.ru:1992");

        alertDialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            boolean companyIdEntered = !TextUtils.isEmpty(companyIdEditText.getText());
            boolean emailEntered = !TextUtils.isEmpty(emailEditText.getText());
            boolean urlEntered = !TextUtils.isEmpty(urlEditText.getText());

            if (companyIdEntered && emailEntered && urlEntered) {
                UsedeskConfiguration configuration = new UsedeskConfiguration(
                        companyIdEditText.getText().toString(),
                        emailEditText.getText().toString(),
                        urlEditText.getText().toString());
                onConfigurationUsedeskListener.onConfigurationUsedeskSet(configuration);
            }

            dismiss();
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dismiss());

        return alertDialogBuilder.create();
    }

    public interface OnConfigurationUsedeskListener {

        void onConfigurationUsedeskSet(UsedeskConfiguration usedeskConfiguration);
    }
}