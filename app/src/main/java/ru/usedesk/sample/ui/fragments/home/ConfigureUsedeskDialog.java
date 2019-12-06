package ru.usedesk.sample.ui.fragments.home;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import ru.usedesk.sample.ui.main.MainActivity;
import ru.usedesk.sdk.external.AppSession;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.knowledgebase.KnowledgeBaseConfiguration;

public class ConfigureUsedeskDialog extends DialogFragment {

    private static final String APP_CONFIGURATION_PREF = "AppConfigure";
    private static final String COMPANY_ID_KEY = "companyId";
    private static final String EMAIL_KEY = "email";
    private static final String URL_KEY = "url";
    private static final String OFFLINE_URL_KEY = "offlineUrl";
    private static final String ACCOUNT_ID_KEY = "accountId";
    private static final String TOKEN_KEY = "token";
    private static final String FOREGROUND_KEY = "foreground";
    private static final String CUSTOM_VIEWS_KEY = "customViews";
    private static final String KNOWLEDGE_BASE_KEY = "knowledgeBase";

    private EditText companyIdEditText;
    private EditText emailEditText;
    private EditText urlEditText;
    private EditText offlineUrlEditText;
    private EditText accountIdEditText;
    private EditText tokenEditText;
    private Switch foregroundSwitch;
    private Switch customViewsSwitch;
    private Switch knowledgeBaseSwitch;

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
        customViewsSwitch = view.findViewById(R.id.switch_custom_views);
        knowledgeBaseSwitch = view.findViewById(R.id.switch_knowledge_base);

        //TODO: установите свои значения
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(APP_CONFIGURATION_PREF,
                Context.MODE_PRIVATE);


        String companyId = sharedPreferences.getString(COMPANY_ID_KEY, "153712");
        String email = sharedPreferences.getString(EMAIL_KEY, "android_sdk@usedesk.ru");
        String url = sharedPreferences.getString(URL_KEY, "https://pubsub.usedesk.ru:1992");
        String offlineUrl = sharedPreferences.getString(OFFLINE_URL_KEY, "https://secure.usedesk.ru");
        String accountId = sharedPreferences.getString(ACCOUNT_ID_KEY, "4");
        String token = sharedPreferences.getString(TOKEN_KEY, "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75");
        boolean foreground = sharedPreferences.getBoolean(FOREGROUND_KEY, true);
        boolean customViews = sharedPreferences.getBoolean(CUSTOM_VIEWS_KEY, false);
        boolean knowledgeBase = sharedPreferences.getBoolean(KNOWLEDGE_BASE_KEY, true);

        companyIdEditText.setText(companyId);
        emailEditText.setText(email);
        urlEditText.setText(url);
        offlineUrlEditText.setText(offlineUrl);
        accountIdEditText.setText(accountId);
        tokenEditText.setText(token);
        foregroundSwitch.setChecked(foreground);
        customViewsSwitch.setChecked(customViews);
        knowledgeBaseSwitch.setChecked(knowledgeBase);

        alertDialogBuilder.setPositiveButton(android.R.string.ok, this::onAcceptClick);

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

    private void onAcceptClick(DialogInterface dialog, int which) {
        String companyId = companyIdEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String url = urlEditText.getText().toString();
        String offlineUrl = offlineUrlEditText.getText().toString();
        String accountId = accountIdEditText.getText().toString();
        String token = tokenEditText.getText().toString();
        boolean foreground = foregroundSwitch.isChecked();
        boolean customViews = customViewsSwitch.isChecked();
        boolean knowledgeBase = knowledgeBaseSwitch.isChecked();

        getContext().getSharedPreferences(APP_CONFIGURATION_PREF, Context.MODE_PRIVATE)
                .edit()
                .putString(COMPANY_ID_KEY, companyId)
                .putString(EMAIL_KEY, email)
                .putString(URL_KEY, url)
                .putString(OFFLINE_URL_KEY, offlineUrl)
                .putString(ACCOUNT_ID_KEY, accountId)
                .putString(TOKEN_KEY, token)
                .putBoolean(FOREGROUND_KEY, foreground)
                .putBoolean(CUSTOM_VIEWS_KEY, customViews)
                .putBoolean(KNOWLEDGE_BASE_KEY, knowledgeBase)
                .apply();

        ((MainActivity) getActivity()).setKnowledgeBase(knowledgeBase);


        boolean companyIdEntered = !TextUtils.isEmpty(companyId);
        boolean emailEntered = !TextUtils.isEmpty(email);
        boolean urlEntered = !TextUtils.isEmpty(url);
        boolean offlineUrlEntered = !TextUtils.isEmpty(offlineUrl);

        if (companyIdEntered && emailEntered && urlEntered && offlineUrlEntered) {
            UsedeskConfiguration configuration = new UsedeskConfiguration(companyId, email, url, offlineUrl);
            onConfigurationUsedeskListener.onConfigurationUsedeskSet(configuration, foreground, customViews, knowledgeBase);

            initKnowledgeBaseConfiguration(accountId, token);

            dismiss();
        } else {
            Toast.makeText(getActivity(), "You must set all parameters", Toast.LENGTH_LONG).show();
        }
    }

    private void initKnowledgeBaseConfiguration(String accountId, String token) {
        UsedeskSdk.initKnowledgeBase(tokenEditText.getContext())
                .setConfiguration(new KnowledgeBaseConfiguration(accountId, token));
        UsedeskSdk.releaseUsedeskKnowledgeBase();
    }

    public interface OnConfigurationUsedeskListener {

        void onConfigurationUsedeskSet(UsedeskConfiguration usedeskConfiguration, boolean foregroundService, boolean customViews, boolean knowledgeBase);
    }
}