package ru.usedesk.sdk.ui.knowledgebase.pages;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.utils.LogUtils;

public class ListViewModel<T> extends ViewModel {

    private static final String TAG = ListViewModel.class.getSimpleName();

    private MutableLiveData<ListOrMessage<T>> liveData = new MutableLiveData<>();
    private Disposable disposable;

    protected ListViewModel() {
        setData(new ListOrMessage<>(ListOrMessage.Message.LOADING));
    }

    public LiveData<ListOrMessage<T>> getLiveData() {
        return liveData;
    }

    protected void loadData(Single<List<T>> single) {
        disposable = single.subscribe(this::onList, this::onThrowable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }

    protected void setData(ListOrMessage<T> listOrMessage) {
        liveData.setValue(listOrMessage);
    }

    private void onList(List<T> list) {
        setData(new ListOrMessage<>(list));
    }

    private void onThrowable(Throwable throwable) {
        LogUtils.LOGE(TAG, throwable);

        setData(new ListOrMessage<>(ListOrMessage.Message.ERROR));
    }
}
