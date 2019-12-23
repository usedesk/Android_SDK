package ru.usedesk.sample.ui.screens.configuration;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import ru.usedesk.sample.DI;
import ru.usedesk.sample.model.configuration.entity.ConfigurationModelo;
import ru.usedesk.sample.model.interactor.ConfigurationInteractor;

public class ConfigurationViewModel extends ViewModel {

    private final ConfigurationInteractor configurationInteractor;

    public ConfigurationViewModel() {
        configurationInteractor = DI.getInstance().getConfigurationInteractor();
    }

    @NonNull
    public ConfigurationModelo getConfigurationModel() {
        return configurationInteractor.getConfigurationModelo();
    }
}
