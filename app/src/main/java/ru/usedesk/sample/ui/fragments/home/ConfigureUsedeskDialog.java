package ru.usedesk.sample.ui.fragments.home;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import ru.usedesk.sample.R;
import ru.usedesk.sdk.external.AppSession;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.knowledgebase.KnowledgeBaseConfiguration;

public class ConfigureUsedeskDialog extends DialogFragment {

    private EditText companyIdEditText;
    private EditText emailEditText;
    private EditText urlEditText;
    private EditText offlineUrlEditText;
    private EditText accountIdEditText;
    private EditText tokenEditText;
    private Switch foregroundSwitch;

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
        offlineUrlEditText = view.findViewById(R.id.offline_url_edit_text);
        accountIdEditText = view.findViewById(R.id.et_account_id);
        tokenEditText = view.findViewById(R.id.et_token);
        foregroundSwitch = view.findViewById(R.id.switch_foreground);

        //TODO: установите свои значения, если требуется
        companyIdEditText.setText("153712");
        emailEditText.setText("android_sdk@usedesk.ru");
        urlEditText.setText("https://pubsub.usedesk.ru:1992");
        offlineUrlEditText.setText("https://secure.usedesk.ru");
        accountIdEditText.setText("4");
        tokenEditText.setText("11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75");

        alertDialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            boolean companyIdEntered = !TextUtils.isEmpty(companyIdEditText.getText());
            boolean emailEntered = !TextUtils.isEmpty(emailEditText.getText());
            boolean urlEntered = !TextUtils.isEmpty(urlEditText.getText());
            boolean offlineUrlEntered = !TextUtils.isEmpty(offlineUrlEditText.getText());

            if (companyIdEntered && emailEntered && urlEntered && offlineUrlEntered) {
                UsedeskConfiguration configuration = new UsedeskConfiguration(
                        companyIdEditText.getText().toString(),
                        emailEditText.getText().toString(),
                        urlEditText.getText().toString(),
                        offlineUrlEditText.getText().toString());
                onConfigurationUsedeskListener.onConfigurationUsedeskSet(configuration, foregroundSwitch.isChecked());

                initKnowledgeBaseConfiguration();

                dismiss();
            } else {
                Toast.makeText(getActivity(), "You must set all parameters", Toast.LENGTH_LONG).show();
            }
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    if (AppSession.getSession().getUsedeskConfiguration() == null) {
                        getActivity().finish();
                    } else {
                        dismiss();
                    }
                }
        );

        return alertDialogBuilder.create();
    }

    private void initKnowledgeBaseConfiguration() {
        String accountId = accountIdEditText.getText().toString();
        String token = tokenEditText.getText().toString();

        UsedeskSdk.initKnowledgeBase(tokenEditText.getContext())
                .setConfiguration(new KnowledgeBaseConfiguration(accountId, token));
        UsedeskSdk.releaseUsedeskKnowledgeBase();
    }

    public interface OnConfigurationUsedeskListener {

        void onConfigurationUsedeskSet(UsedeskConfiguration usedeskConfiguration, boolean foregroundService);
    }
}