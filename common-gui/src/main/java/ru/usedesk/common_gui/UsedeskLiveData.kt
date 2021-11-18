package ru.usedesk.common_gui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

class UsedeskLiveData<T>(
    private val default: T
) {
    private val liveData = MutableLiveData<T?>(default)

    var value: T
        set(value) {
            liveData.value = value
        }
        get() {
            return liveData.value ?: default
        }

    fun postValue(value: T) {
        liveData.postValue(value)
    }

    fun initAndObserve(lifecycleOwner: LifecycleOwner, onValue: (T) -> Unit) {
        var old = value
        onValue(old)
        liveData.observe(lifecycleOwner) {
            if (it != null && old != it) {
                old = it
                onValue(it)
            }
        }
    }

    fun initAndObserveWithOld(lifecycleOwner: LifecycleOwner, onData: (T?, T) -> Unit) {
        var old: T? = null
        initAndObserve(lifecycleOwner) {
            if (it != null) {
                onData(old, it)
                old = it
            }
        }
    }
}