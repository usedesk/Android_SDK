package ru.usedesk.sample;

import android.content.Context;

import androidx.annotation.NonNull;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;
import ru.usedesk.sample.model.configuration.repository.ConfigurationValidator;

public class DI {
    private static DI instance = null;

    private final ConfigurationRepository configurationRepository;
    private final ConfigurationValidator configurationValidator;

    private DI(@NonNull Context appContext) {
        configurationRepository = new ConfigurationRepository(appContext.getSharedPreferences(ConfigurationRepository.class.getName(), Context.MODE_PRIVATE));
        configurationValidator = new ConfigurationValidator(appContext.getResources());
    }

    static void init(@NonNull Context appContext) {
        if (instance == null) {
            instance = new DI(appContext);
        }
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

    public Scheduler getWorkScheduler() {
        return Schedulers.computation();
    }
}
