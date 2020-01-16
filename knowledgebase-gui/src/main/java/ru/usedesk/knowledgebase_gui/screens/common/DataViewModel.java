package ru.usedesk.knowledgebase_gui.screens.common;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.usedesk.knowledgebase_gui.screens.entity.DataOrMessage;

public class DataViewModel<T> extends ViewModel {

    private MutableLiveData<DataOrMessage<T>> liveData = new MutableLiveData<>();
    private Disposable disposable;

    protected DataViewModel() {
        setData(new DataOrMessage<>(DataOrMessage.Message.LOADING));
    }

    public LiveData<DataOrMessage<T>> getLiveData() {
        return liveData;
    }

    protected void loadData(Single<T> single) {
        disposable = single.subscribe(this::onData, this::onThrowable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    protected void setData(DataOrMessage<T> DataOrMessage) {
        liveData.setValue(DataOrMessage);
    }

    protected void onData(T data) {
        setData(new DataOrMessage<>(data));
    }

    private void onThrowable(Throwable throwable) {
        setData(new DataOrMessage<>(DataOrMessage.Message.ERROR));
    }
}
