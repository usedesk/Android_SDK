package ru.usedesk.common_gui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.updateAndGet

open class UsedeskViewModel<MODEL>(
    defaultModel: MODEL
) : ViewModel() {
    private val _modelFlow = MutableStateFlow(defaultModel)
    val modelFlow: StateFlow<MODEL> = _modelFlow

    private var inited = false

    protected val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    protected val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    protected fun doInit(init: () -> Unit) {
        if (!inited) {
            inited = true
            init()
        }
    }

    protected fun setModel(onUpdate: MODEL.() -> MODEL) = _modelFlow.updateAndGet { it.onUpdate() }

    override fun onCleared() {
        super.onCleared()

        ioScope.cancel()
        mainScope.cancel()
    }
}