package ru.usedesk.sample.ui.screens.configuration;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputLayout;

import ru.usedesk.chat_sdk.UsedeskChatSdk;
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration;
import ru.usedesk.common_sdk.entity.UsedeskEvent;
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration;
import ru.usedesk.sample.R;
import ru.usedesk.sample.databinding.ScreenConfigurationBinding;
import ru.usedesk.sample.model.configuration.entity.Configuration;
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation;

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

        binding.switchForeground.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UsedeskChatSdk.stopService(requireContext());
        });

        try {
            String version = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            binding.tvVersion.setText("v" + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        initTil(binding.tilUrlChat);
        initTil(binding.tilUrlOfflineForm);
        initTil(binding.tilUrlToSendFile);
        initTil(binding.tilUrlApi);
        initTil(binding.tilCompanyId);
        initTil(binding.tilAccountId);
        initTil(binding.tilToken);
        initTil(binding.tilClientEmail);
        initTil(binding.tilClientPhoneNumber);

        return binding.getRoot();
    }

    @Override
    public void onPause() {
        super.onPause();

        viewModel.setTempConfiguration(getConfiguration());
    }

    private void onGoToSdkEvent(@NonNull UsedeskEvent<Object> event) {
        event.process(data -> {
            ((IOnGoToSdkListener) getActivity()).goToSdk();
            return null;
        });
    }

    private void onGoToSdk() {
        viewModel.onGoSdkClick(getConfiguration());
    }

    private Configuration getConfiguration() {
        return new Configuration(
                binding.etUrlChat.getText().toString(),
                binding.etUrlOfflineForm.getText().toString(),
                binding.etUrlToSendFile.getText().toString(),
                binding.etUrlApi.getText().toString(),
                binding.etCompanyId.getText().toString(),
                binding.etAccountId.getText().toString(),
                binding.etToken.getText().toString(),
                binding.etClientSignature.getText().toString(),
                binding.etClientEmail.getText().toString(),
                binding.etClientName.getText().toString(),
                binding.etClientNote.getText().toString(),
                getLong(binding.etClientPhoneNumber.getText().toString()),
                getLong(binding.etClientAdditionalId.getText().toString()),
                binding.etClientInitMessage.getText().toString(),
                binding.etCustomAgentName.getText().toString(),
                binding.switchForeground.isChecked(),
                binding.switchKnowledgeBase.isChecked(),
                binding.switchWithSupportButton.isChecked());
    }

    @Nullable
    private Long getLong(@Nullable String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private void onNewConfiguration(@NonNull Configuration configuration) {
        binding.etUrlChat.setText(configuration.getUrlChat());
        binding.etUrlOfflineForm.setText(configuration.getUrlOfflineForm());
        binding.etUrlToSendFile.setText(configuration.getUrlToSendFile());
        binding.etUrlApi.setText(configuration.getUrlApi());
        binding.etCompanyId.setText(configuration.getCompanyId());
        binding.etAccountId.setText(configuration.getAccountId());
        binding.etToken.setText(configuration.getToken());
        binding.etClientSignature.setText(configuration.getClientSignature());
        binding.etClientEmail.setText(configuration.getClientEmail());
        binding.etClientName.setText(configuration.getClientName());
        String clientPhoneNumber;
        if (configuration.getClientPhoneNumber() == null) {
            clientPhoneNumber = "";
        } else {
            clientPhoneNumber = configuration.getClientPhoneNumber().toString();
        }
        binding.etClientPhoneNumber.setText(clientPhoneNumber);
        String clientAdditionalId;
        if (configuration.getClientAdditionalId() == null) {
            clientAdditionalId = "";
        } else {
            clientAdditionalId = configuration.getClientAdditionalId().toString();
        }
        binding.etClientAdditionalId.setText(clientAdditionalId);
        binding.etClientInitMessage.setText(configuration.getClientInitMessage());
        binding.etCustomAgentName.setText(configuration.getCustomAgentName());
        binding.switchForeground.setChecked(configuration.getForegroundService());
        binding.switchKnowledgeBase.setChecked(configuration.getWithKnowledgeBase());

        viewModel.getConfiguration().removeObservers(getViewLifecycleOwner());
    }

    private void initTil(TextInputLayout inputLayout) {
        inputLayout.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                inputLayout.setError(null);
            }
        });
        inputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputLayout.setError(null);
            }
        });
    }

    private void showError(@NonNull TextInputLayout textInputLayout,
                           boolean isValid,
                           int errorStringId) {
        if (isValid) {
            textInputLayout.setError(null);
        } else {
            textInputLayout.setError(getResources().getString(errorStringId));
        }
    }

    private void onNewConfigurationValidation(
            @NonNull ConfigurationValidation configurationValidation
    ) {
        UsedeskChatConfiguration.Validation chatValidation =
                configurationValidation.getChatConfigurationValidation();

        showError(binding.tilUrlChat,
                chatValidation.getValidUrlChat(),
                R.string.validation_url_error);

        showError(binding.tilUrlOfflineForm,
                chatValidation.getValidUrlOfflineForm(),
                R.string.validation_url_error);

        showError(binding.tilUrlToSendFile,
                chatValidation.getValidUrlToSendFile(),
                R.string.validation_url_error);

        showError(binding.tilCompanyId,
                chatValidation.getValidCompanyId(),
                R.string.validation_empty_error);

        showError(binding.tilClientEmail,
                chatValidation.getValidClientEmail(),
                R.string.validation_email_error);

        showError(binding.tilClientPhoneNumber,
                chatValidation.getValidClientPhoneNumber(),
                R.string.validation_phone_error);

        UsedeskKnowledgeBaseConfiguration.Validation knowledgebaseValidation =
                configurationValidation.getKnowledgeBaseConfiguration();

        showError(binding.tilUrlApi,
                knowledgebaseValidation.getValidUrlApi(),
                R.string.validation_empty_error);

        showError(binding.tilAccountId,
                knowledgebaseValidation.getValidAccountId(),
                R.string.validation_empty_error);

        showError(binding.tilToken,
                knowledgebaseValidation.getValidToken(),
                R.string.validation_empty_error);
    }

    public interface IOnGoToSdkListener {
        void goToSdk();
    }
}
