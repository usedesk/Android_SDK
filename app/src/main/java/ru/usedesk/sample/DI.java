package ru.usedesk.sample;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;
import ru.usedesk.sample.model.configuration.repository.ConfigurationValidator;
import ru.usedesk.sample.model.interactor.ConfigurationInteractor;

public class DI {
    public static DI instance;

    private final ConfigurationRepository configurationRepository;
    private final ConfigurationValidator configurationValidator;
    private final ConfigurationInteractor configurationInteractor;

    public DI(@NonNull Context appContext) {
        configurationRepository = new ConfigurationRepository(appContext.getSharedPreferences(ConfigurationRepository.class.getName(), Context.MODE_PRIVATE));
        configurationValidator = new ConfigurationValidator(appContext.getResources());

        configurationInteractor = new ConfigurationInteractor();
    }

    @NonNull
    public static DI getInstance() {
        return instance;
    }

    @NonNull
    public ConfigurationRepository getConfigurationRepository() {
        return configurationRepository;
    }

    @NonNull
    public ConfigurationValidator getConfigurationValidator() {
        return configurationValidator;
    }

    @NonNull
    public ConfigurationInteractor getConfigurationInteractor() {
        return configurationInteractor;
    }
}
