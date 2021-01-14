package ru.usedesk.sample.ui.screens.configuration;

import android.content.pm.PackageManager;
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
import ru.usedesk.sample.databinding.ScreenConfigurationBinding;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation;
import ru.usedesk.sample.ui._common.Event;

public class ConfigurationScreen extends Fragment {

    private ScreenConfigurationBinding binding;
    private ConfigurationViewModel viewModel;

    @NonNull
    public static ConfigurationScreen newInstance() {
        return new ConfigurationScreen();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.screen_configuration,
                container, false);

        viewModel = ViewModelProviders.of(this).get(ConfigurationViewModel.class);

        viewModel.getConfiguration()
                .observe(getViewLifecycleOwner(), this::onNewConfiguration);

        viewModel.getConfigurationValidation()
                .observe(getViewLifecycleOwner(), this::onNewConfigurationValidation);

        viewModel.getGoToSdkEvent()
                .observe(getViewLifecycleOwner(), this::onGoToSdkEvent);

        binding.btnGoToSdk.setOnClickListener(v -> onGoToSdk());

        try {
            String version = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            binding.tvVersion.setText("v" + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();

        viewModel.setTempConfiguration(getConfiguration());
    }

    private void onGoToSdkEvent(@NonNull Event<Object> event) {
        event.doEvent(obj -> ((IOnGoToSdkListener) getActivity()).goToSdk());
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
                binding.etInitClientMessage.getText().toString(),
                binding.etCustomAgentName.getText().toString(),
                binding.switchForeground.isChecked(),
                binding.switchKnowledgeBase.isChecked());
    }

    private void onNewConfiguration(@NonNull Configuration configuration) {
        binding.etCompanyId.setText(configuration.getCompanyId());
        binding.etEmail.setText(configuration.getClientEmail());
        binding.etUrl.setText(configuration.getUrl());
        binding.etOfflineUrl.setText(configuration.getOfflineFormUrl());
        binding.etAccountId.setText(configuration.getAccountId());
        binding.etToken.setText(configuration.getToken());
        binding.etClientName.setText(configuration.getClientName());
        binding.etClientPhoneNumber.setText(configuration.getClientPhoneNumber());
        binding.etClientAdditionalId.setText(configuration.getClientAdditionalId());
        binding.etInitClientMessage.setText(configuration.getInitClientMessage());
        binding.etCustomAgentName.setText(configuration.getCustomAgentName());
        binding.switchForeground.setChecked(configuration.isForegroundService());
        binding.switchKnowledgeBase.setChecked(configuration.isWithKnowledgeBase());

        viewModel.getConfiguration().removeObservers(getViewLifecycleOwner());
    }

    private void onNewConfigurationValidation(@NonNull ConfigurationValidation configurationValidation) {
        binding.tilCompanyId.setError(configurationValidation.getCompanyIdError());
        binding.tilEmail.setError(configurationValidation.getEmailError());
        binding.tilClientPhoneNumber.setError(configurationValidation.getPhoneNumberError());
        binding.tilUrl.setError(configurationValidation.getUrlError());
        binding.tilOfflineUrl.setError(configurationValidation.getOfflineFormUrlError());
        binding.tilAccountId.setError(configurationValidation.getAccountIdError());
        binding.tilToken.setError(configurationValidation.getTokenError());
    }

    public interface IOnGoToSdkListener {
        void goToSdk();
    }
}
