package ru.usedesk.sample.ui.fragments.configure;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ConfigureViewModel extends ViewModel {

    private final ConfigureRepository configureRepository;
    private final MutableLiveData<ConfigureModel> liveData = new MutableLiveData<>();

    public ConfigureViewModel() {
        configureRepository = new ConfigureRepository();

        liveData.postValue(configureRepository.getConfigureModel());
    }

    @NonNull
    public LiveData<ConfigureModel> getConfigureModule() {
        return liveData;
    }

    public void onSave(@Nullable Long companyId,
                       @NonNull String email,
                       @NonNull String url,
                       @NonNull String offlineFormUrl,
                       @Nullable Long accountId,
                       @NonNull String token,
                       @NonNull String clientName,
                       @NonNull String clientPhoneNumber,
                       @NonNull String clientAdditionalId,
                       @NonNull Boolean foregroundService,
                       @NonNull Boolean customViews,
                       @NonNull Boolean withKnowledgeBase) {
        ConfigureModel configureModel = new ConfigureModel.Builder(null)
                .setCompanyId(companyId)
                .setEmail(email)
                .setUrl(url)
                .setOfflineFormUrl(offlineFormUrl)
                .setAccountId(accountId)
                .setToken(token)
                .setClientName(clientName)
                .setClientPhoneNumber(clientPhoneNumber)
                .setClientAdditionalId(clientAdditionalId)
                .setForegroundService(foregroundService)
                .setCustomViews(customViews)
                .setWithKnowledgeBase(withKnowledgeBase)
                .build();

        configureRepository.setConfigureModel(configureModel);
    }
}
