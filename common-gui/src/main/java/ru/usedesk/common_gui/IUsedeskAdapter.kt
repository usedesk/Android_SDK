package ru.usedesk.common_gui

import androidx.lifecycle.LifecycleOwner

interface IUsedeskAdapter<VM : UsedeskViewModel> {
    fun onLiveData(viewModel: VM, lifecycleOwner: LifecycleOwner)
}