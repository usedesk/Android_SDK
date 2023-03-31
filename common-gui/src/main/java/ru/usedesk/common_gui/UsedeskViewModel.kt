package ru.usedesk.common_gui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.updateAndGet
import ru.usedesk.common_sdk.UsedeskLog

open class UsedeskViewModel<MODEL>(defaultModel: MODEL) : ViewModel() {
    private val _modelFlow = MutableStateFlow(defaultModel)
    val modelFlow: StateFlow<MODEL> = _modelFlow

    protected val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        UsedeskLog.onLog("UsedeskViewModel.init") { this.toString() }
    }

    protected fun <T> StateFlow<T>.launchCollect(onValue: (T) -> Unit) {
        onValue(value)
        mainScope.launch { collect(onValue) }
    }

    protected fun setModel(onUpdate: MODEL.() -> MODEL) = _modelFlow.updateAndGet { it.onUpdate() }

    override fun onCleared() {
        UsedeskLog.onLog("UsedeskViewModel.onCleared") { this.toString() }
        mainScope.cancel()

        super.onCleared()
    }
}