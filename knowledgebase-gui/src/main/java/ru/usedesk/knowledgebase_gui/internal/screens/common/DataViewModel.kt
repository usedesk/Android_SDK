package ru.usedesk.knowledgebase_gui.internal.screens.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import ru.usedesk.knowledgebase_gui.internal.screens.entity.DataOrMessage

open class DataViewModel<T> protected constructor() : ViewModel() {

    val liveData = MutableLiveData<DataOrMessage<T>>()
    private var disposable: Disposable? = null

    init {
        setData(DataOrMessage(DataOrMessage.Message.LOADING))
    }

    fun getLiveData(): LiveData<DataOrMessage<T>> {
        return liveData
    }

    protected fun loadData(single: Single<T>) {
        disposable = single.subscribe({
            onData(it)
        }) {
            onThrowable(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (disposable?.isDisposed != true) {
            disposable?.dispose()
        }
    }

    private fun setData(DataOrMessage: DataOrMessage<T>) {
        liveData.value = DataOrMessage
    }

    protected open fun onData(data: T) {
        setData(DataOrMessage(data))
    }

    private fun onThrowable(throwable: Throwable) {
        setData(DataOrMessage(DataOrMessage.Message.ERROR))
    }
}