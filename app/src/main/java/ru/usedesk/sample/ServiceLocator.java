package ru.usedesk.sample;

import android.content.Context;

import androidx.annotation.NonNull;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.sample.model.configuration.repository.ConfigurationRepository;

public class ServiceLocator {
    private static ServiceLocator instance = null;

    private final ConfigurationRepository configurationRepository;

    private ServiceLocator(@NonNull Context appContext) {
        configurationRepository = new ConfigurationRepository(appContext.getSharedPreferences("SampleConfiguration", Context.MODE_PRIVATE),
                getWorkScheduler());
    }

    static void init(@NonNull Context appContext) {
        if (instance == null) {
            instance = new ServiceLocator(appContext);
        }
    }

    @NonNull
    public static ServiceLocator getInstance() {
        return instance;
    }

    @NonNull
    public ConfigurationRepository getConfigurationRepository() {
        return configurationRepository;
    }

    public Scheduler getWorkScheduler() {
        return Schedulers.io();
    }
}
