package ru.usedesk.sdk.external.ui;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

public abstract class UsedeskViewModel<T> extends ViewModel {

    private MutableLiveData<T> modelLiveData = new MutableLiveData<>();
    private T oldModel;

    protected void onNewModel(@NonNull T newModel) {
        oldModel = oldModel == null//TODO: реализовать reduce через rx
                ? newModel
                : reduceModels(oldModel, newModel);
        modelLiveData.postValue(oldModel);
    }

    @NonNull
    protected abstract T reduceModels(@NonNull T oldModel, @NonNull T newModel);

    public MutableLiveData<T> getModelLiveData() {
        return modelLiveData;
    }
}
