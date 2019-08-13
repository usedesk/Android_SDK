package ru.usedesk.sdk.external.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

public abstract class MviViewModel<T extends Reducable<T>> extends ViewModel {

    private MutableLiveData<T> modelLiveData = new MutableLiveData<>();

    public MviViewModel(@NonNull T model) {
        modelLiveData.setValue(model);
    }

    protected void onNewModel(@NonNull T newModel) {
        modelLiveData.postValue(modelLiveData.getValue().reduce(newModel));
    }

    public MutableLiveData<T> getModelLiveData() {
        return modelLiveData;
    }
}
