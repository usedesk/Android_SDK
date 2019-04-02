package ru.usedesk.sample.ui.main;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Navigate> navigateLiveData = new MutableLiveData<>();

    public MainViewModel() {
        navigateLiveData.setValue(Navigate.HOME);
    }

    MutableLiveData<Navigate> getNavigateLiveData() {
        return navigateLiveData;
    }

    void onNavigate(Navigate navigate) {
        navigateLiveData.setValue(navigate);
    }

    public enum Navigate {
        HOME,
        BASE,
        INFO
    }
}
