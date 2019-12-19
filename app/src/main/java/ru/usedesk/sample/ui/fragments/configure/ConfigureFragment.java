package ru.usedesk.sample.ui.fragments.configure;

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

public class ConfigureFragment extends Fragment {

    private FragmentConfigureBinding binding;
    private ConfigureViewModel viewModel;

    @NonNull
    public static ConfigureFragment newInstance() {
        return new ConfigureFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_configure, container, false);

        viewModel = ViewModelProviders.of(this).get(ConfigureViewModel.class);

        viewModel.getConfigureModule()
                .observe(this, this::onNewModel);

        return binding.getRoot();
    }

    private void onNewModel(@NonNull ConfigureModel configureModel) {
        binding.companyIdEditText.setText(toString(configureModel.getCompanyId()));
        binding.emailEditText.setText(configureModel.getEmail());
        binding.urlEditText.setText(configureModel.getUrl());
        binding.offlineUrlEditText.setText(configureModel.getOfflineFormUrl());
        binding.etAccountId.setText(toString(configureModel.getAccountId()));
        binding.etToken.setText(configureModel.getToken());
        binding.etClientName.setText(configureModel.getClientName());
        binding.etClientPhone.setText(configureModel.getClientPhoneNumber());
        binding.etClientAdditionalId.setText(configureModel.getClientAdditionalId());
        binding.switchForeground.setChecked(toBoolean(configureModel.getForegroundService()));
        binding.switchCustomViews.setChecked(toBoolean(configureModel.getCustomViews()));
        binding.switchKnowledgeBase.setChecked(toBoolean(configureModel.getWithKnowledgeBase()));
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
}
