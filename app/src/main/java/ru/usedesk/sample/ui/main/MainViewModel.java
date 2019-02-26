package ru.usedesk.sample.ui.main;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    static final int NAVIGATE_HOME = 1;
    static final int NAVIGATE_BASE = 2;
    static final int NAVIGATE_INFO = 3;

    private final MutableLiveData<Integer> navigateLiveData = new MutableLiveData<>();

    public MainViewModel() {
        navigateLiveData.setValue(NAVIGATE_HOME);
    }

    MutableLiveData<Integer> getNavigateLiveData() {
        return navigateLiveData;
    }

    void onNavigate(int navigateId) {
        navigateLiveData.setValue(navigateId);
    }
}
