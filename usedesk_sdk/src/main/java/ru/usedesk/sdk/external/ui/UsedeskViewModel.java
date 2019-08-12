package ru.usedesk.sdk.external.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

public abstract class UsedeskViewModel<T extends Reducable<T>> extends ViewModel {

    private MutableLiveData<T> modelLiveData = new MutableLiveData<>();
    private T oldModel;

    protected void onNewModel(@NonNull T newModel) {
        oldModel = oldModel == null//TODO: реализовать reduce через rx
                ? newModel
                : newModel.reduce(oldModel);
        modelLiveData.postValue(oldModel);
    }

    public MutableLiveData<T> getModelLiveData() {
        return modelLiveData;
    }
}
