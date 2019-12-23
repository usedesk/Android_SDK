package ru.usedesk.sample.ui.screens.configuration;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.usedesk.sample.R;
import ru.usedesk.sample.databinding.FragmentConfigureBinding;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.entity.ConfigurationModelo;
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidationModel;

public class ConfigurationFragment extends Fragment {

    private FragmentConfigureBinding binding;
    private ConfigurationViewModel viewModel;

    @NonNull
    public static ConfigurationFragment newInstance() {
        return new ConfigurationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_configure, container, false);

        viewModel = ViewModelProviders.of(this).get(ConfigurationViewModel.class);

        ConfigurationModelo configurationModel = viewModel.getConfigurationModel();

        binding.btnGoToSdk.setOnClickListener(v -> configurationModel.setIntent(ConfigurationModelo.IntentKey.EVENT_SET_CONFIGURATION, ""));
        configurationModel.getData();

        return binding.getRoot();
    }

    private void onGoToSdk() {
        renderValidationErrors(new ConfigurationValidationModel());

        viewModel.onGoToSdk(binding.etCompanyId.getText().toString(),
                binding.etEmail.getText().toString(),
                binding.etUrl.getText().toString(),
                binding.etOfflineUrl.getText().toString(),
                binding.etAccountId.getText().toString(),
                binding.etToken.getText().toString(),
                binding.etClientName.getText().toString(),
                binding.etClientPhoneNumber.getText().toString(),
                binding.etClientAdditionalId.getText().toString(),
                binding.switchForeground.isChecked(),
                binding.switchCustomViews.isChecked(),
                binding.switchKnowledgeBase.isChecked());
    }

    private void onNewConfigureModel(@NonNull Configuration configurationModel) {
        binding.etCompanyId.setText(toString(configurationModel.getCompanyId()));
        binding.etEmail.setText(configurationModel.getEmail());
        binding.etUrl.setText(configurationModel.getUrl());
        binding.etOfflineUrl.setText(configurationModel.getOfflineFormUrl());
        binding.etAccountId.setText(toString(configurationModel.getAccountId()));
        binding.etToken.setText(configurationModel.getToken());
        binding.etClientName.setText(configurationModel.getClientName());
        binding.etClientPhoneNumber.setText(configurationModel.getClientPhoneNumber());
        binding.etClientAdditionalId.setText(configurationModel.getClientAdditionalId());
        binding.switchForeground.setChecked(toBoolean(configurationModel.isForegroundService()));
        binding.switchCustomViews.setChecked(toBoolean(configurationModel.isCustomViews()));
        binding.switchKnowledgeBase.setChecked(toBoolean(configurationModel.isWithKnowledgeBase()));
    }

    private void onNewConfigureValidateionModel(@NonNull ConfigurationValidationModel configurationValidationModel) {
        if (configurationValidationModel.isSuccessed()) {
            ((IOnGoToSdkListener) getActivity()).goToSdk();
        } else {
            renderValidationErrors(configurationValidationModel);
        }
    }

    private void renderValidationErrors(@NonNull ConfigurationValidationModel configurationValidationModel) {
        binding.etCompanyId.setError(configurationValidationModel.getCompanyIdError());
        binding.etEmail.setError(configurationValidationModel.getEmailError());
        binding.etUrl.setError(configurationValidationModel.getUrlError());
        binding.etOfflineUrl.setError(configurationValidationModel.getOfflineFormUrlError());
        binding.etAccountId.setError(configurationValidationModel.getAccountIdError());
        binding.etToken.setError(configurationValidationModel.getTokenError());
    }

    @NonNull
    private String toString(@Nullable Object value) {
        return value == null
                ? ""
                : value.toString();
    }

    @NonNull
    private Boolean toBoolean(@Nullable Boolean bool) {
        return bool == null
                ? false
                : bool;
    }

    public interface IOnGoToSdkListener {
        void goToSdk();
    }
}
