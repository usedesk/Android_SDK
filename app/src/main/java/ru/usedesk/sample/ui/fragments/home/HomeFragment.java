package ru.usedesk.sample.ui.fragments.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ru.usedesk.sample.R;
import ru.usedesk.sample.service.CustomForegroundNotificationsService;
import ru.usedesk.sample.service.CustomSimpleNotificationsService;
import ru.usedesk.sdk.external.AppSession;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.ui.UsedeskViewCustomizer;

public class HomeFragment extends Fragment implements ConfigureUsedeskDialog.OnConfigurationUsedeskListener {

    private TextView noConfigurationTextView;
    private Button actionButton;
    private TextView companyIdTextView;
    private TextView emailTextView;
    private TextView urlTextView;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppSession.getSession() == null) {
            showEmailDialog();
        } else {
            updateUI();
        }
    }

    @Override
    public void onConfigurationUsedeskSet(UsedeskConfiguration usedeskConfiguration,
                                          boolean foregroundService, boolean customViews) {
        AppSession.startSession(usedeskConfiguration);
        updateUI();

        UsedeskSdk.setUsedeskNotificationsServiceFactory(foregroundService
                ? new CustomForegroundNotificationsService.Factory()
                : new CustomSimpleNotificationsService.Factory());

        UsedeskViewCustomizer usedeskViewCustomizer = UsedeskSdk.getUsedeskViewCustomizer();
        if (customViews) {
            //Полная замена фрагментов кастомными
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_category, R.layout.custom_item_category);
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_section, R.layout.custom_item_section);
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_article_info, R.layout.custom_item_article_info);

            //Применение кастомной темы к стандартным фрагментам
            usedeskViewCustomizer.setThemeId(R.style.Usedesk_Theme_Custom);
        } else {
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_category, ru.usedesk.sdk.R.layout.usedesk_item_category);
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_section, ru.usedesk.sdk.R.layout.usedesk_item_section);
            usedeskViewCustomizer.setLayoutId(ru.usedesk.sdk.R.layout.usedesk_item_article_info, ru.usedesk.sdk.R.layout.usedesk_item_article_info);

            usedeskViewCustomizer.setThemeId(R.style.Usedesk_Theme);
        }
    }

    private void initUI(View view) {
        noConfigurationTextView = view.findViewById(R.id.no_configuration_text_view);

        actionButton = view.findViewById(R.id.action_button);
        actionButton.setOnClickListener(view1 -> showEmailDialog());

        companyIdTextView = view.findViewById(R.id.company_id_text_view);
        emailTextView = view.findViewById(R.id.email_text_view);
        urlTextView = view.findViewById(R.id.url_text_view);
    }

    private void updateUI() {
        if (AppSession.getSession() != null) {
            UsedeskConfiguration usedeskConfiguration = AppSession.getSession().getUsedeskConfiguration();

            noConfigurationTextView.setVisibility(View.GONE);

            actionButton.setText(R.string.configuration_set_configuration);

            companyIdTextView.setVisibility(View.VISIBLE);
            companyIdTextView.setText(getString(R.string.configuration_company_id, usedeskConfiguration.getCompanyId()));

            emailTextView.setVisibility(View.VISIBLE);
            emailTextView.setText(getString(R.string.configuration_email, usedeskConfiguration.getEmail()));

            urlTextView.setVisibility(View.VISIBLE);
            urlTextView.setText(getString(R.string.configuration_url, usedeskConfiguration.getUrl()));
        } else {
            noConfigurationTextView.setVisibility(View.VISIBLE);

            actionButton.setText(R.string.configuration_configure);

            companyIdTextView.setVisibility(View.GONE);

            emailTextView.setVisibility(View.GONE);

            urlTextView.setVisibility(View.GONE);
        }
    }

    private void showEmailDialog() {
        if (getFragmentManager().findFragmentByTag(ConfigureUsedeskDialog.class.getSimpleName()) == null) {
            ConfigureUsedeskDialog.newInstance(this)
                    .show(getFragmentManager(), ConfigureUsedeskDialog.class.getSimpleName());
        }
    }
}