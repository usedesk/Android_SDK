package ru.usedesk.sample.ui._common;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

public interface IOnTextLiveData {
    @NonNull
    LiveData<String> getLiveData();
}
