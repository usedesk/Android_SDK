package ru.usedesk.chat_gui.screens.chat;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public abstract class MviViewModel<T extends ReducibleModel<T>> extends ViewModel {

    private Disposable disposable;
    private MutableLiveData<T> modelLiveData = new MutableLiveData<>();
    private Observable<T> modelObservable;

    public MviViewModel(@NonNull T model) {
        modelLiveData.setValue(model);
    }

    protected void onNewModel(@NonNull T newModel) {
        modelLiveData.postValue(getLastModel().reduce(newModel));
    }

    protected void addModelObservable(@NonNull Observable<T> modelObservable) {
        if (this.modelObservable == null) {
            this.modelObservable = modelObservable;
        } else {
            this.modelObservable = Observable.merge(this.modelObservable, modelObservable);
        }
    }

    protected void initLiveData(@NonNull Consumer<Throwable> throwableConsumer) {
        disposable = modelObservable.subscribe(this::onNewModel, throwableConsumer);
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    protected T getLastModel() {
        return modelLiveData.getValue();
    }

    public MutableLiveData<T> getModelLiveData() {
        return modelLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }
}
