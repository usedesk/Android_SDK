package ru.usedesk.sdk.ui.knowledgebase;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.utils.LogUtils;

public class DataViewModel<T> extends ViewModel {

    private static final String TAG = DataViewModel.class.getSimpleName();

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

        disposable.dispose();
    }

    protected void setData(DataOrMessage<T> DataOrMessage) {
        liveData.setValue(DataOrMessage);
    }

    private void onData(T data) {
        setData(new DataOrMessage<>(data));
    }

    private void onThrowable(Throwable throwable) {
        LogUtils.LOGE(TAG, throwable);

        setData(new DataOrMessage<>(DataOrMessage.Message.ERROR));
    }
}
