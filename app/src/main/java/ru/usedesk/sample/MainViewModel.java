package ru.usedesk.sample;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

class MainViewModel extends ViewModel {

    static final int NAVIGATE_HOME = 1;
    static final int NAVIGATE_BASE = 2;
    static final int NAVIGATE_INFO = 3;

    private final MutableLiveData<Integer> navigateLiveData = new MutableLiveData<>();

    MainViewModel() {
        navigateLiveData.setValue(NAVIGATE_HOME);
    }

    MutableLiveData<Integer> getNavigateLiveData() {
        return navigateLiveData;
    }

    void onNavigate(int navigateId) {
        navigateLiveData.setValue(navigateId);
    }
}
