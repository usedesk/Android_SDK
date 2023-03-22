package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal inline fun <reified T : ViewModel> composeViewModel(
    key: String? = null,
    viewModelStoreOwner: ViewModelStoreOwner,
    crossinline viewModelInstanceCreator: () -> T
): T = viewModel(
    modelClass = T::class.java,
    key = key,
    viewModelStoreOwner = viewModelStoreOwner,
    factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            viewModelInstanceCreator() as T
    }
)