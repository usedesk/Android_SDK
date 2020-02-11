package ru.usedesk.sample.ui.screens.configuration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import ru.usedesk.sample.R;
import ru.usedesk.sample.databinding.FragmentConfigureBinding;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation;
import ru.usedesk.sample.ui._common.Event;

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

        viewModel.getConfiguration()
                .observe(this, this::onNewConfigure);

        viewModel.getConfigurationValidation()
                .observe(this, this::onNewConfigureValidation);

        viewModel.getGoToSdkEvent()
                .observe(this, this::onGoToSdkEvent);

        binding.btnGoToSdk.setOnClickListener(v -> onGoToSdk());

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();

        viewModel.setTempConfiguration(getConfiguration());
    }

    @SuppressWarnings("ConstantConditions")
    private void onGoToSdkEvent(@NonNull Event event) {
        if (!event.isProcessed()) {
            ((IOnGoToSdkListener) getActivity()).goToSdk();
        }
    }

    private void onGoToSdk() {
        viewModel.onGoSdkClick(getConfiguration());
    }

    private Configuration getConfiguration() {
        return new Configuration(binding.etCompanyId.getText().toString(),
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

    private void onNewConfigure(@NonNull Configuration configuration) {
        binding.etCompanyId.setText(configuration.getCompanyId());
        binding.etEmail.setText(configuration.getEmail());
        binding.etUrl.setText(configuration.getUrl());
        binding.etOfflineUrl.setText(configuration.getOfflineFormUrl());
        binding.etAccountId.setText(configuration.getAccountId());
        binding.etToken.setText(configuration.getToken());
        binding.etClientName.setText(configuration.getClientName());
        binding.etClientPhoneNumber.setText(configuration.getClientPhoneNumber());
        binding.etClientAdditionalId.setText(configuration.getClientAdditionalId());
        binding.switchForeground.setChecked(configuration.isForegroundService());
        binding.switchCustomViews.setChecked(configuration.isCustomViews());
        binding.switchKnowledgeBase.setChecked(configuration.isWithKnowledgeBase());

        viewModel.getConfiguration().removeObservers(this);
    }

    private void onNewConfigureValidation(@NonNull ConfigurationValidation configurationValidation) {
        binding.tilCompanyId.setError(configurationValidation.getCompanyIdError());
        binding.tilEmail.setError(configurationValidation.getEmailError());
        binding.tilUrl.setError(configurationValidation.getUrlError());
        binding.tilOfflineUrl.setError(configurationValidation.getOfflineFormUrlError());
        binding.tilAccountId.setError(configurationValidation.getAccountIdError());
        binding.tilToken.setError(configurationValidation.getTokenError());
    }

    public interface IOnGoToSdkListener {
        void goToSdk();
    }
}
