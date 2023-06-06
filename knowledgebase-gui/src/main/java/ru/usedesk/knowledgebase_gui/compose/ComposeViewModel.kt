package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.usedesk.knowledgebase_gui._di.KbUiComponent

@Composable
internal inline fun <reified T : ViewModel> kbUiViewModel(
    key: Any? = null,
    viewModelStoreOwner: ViewModelStoreOwner,
    factory: KbUiViewModelFactory<T>
): T = viewModel(
    modelClass = T::class.java,
    key = remember(key) { key?.toString() },
    viewModelStoreOwner = viewModelStoreOwner,
    factory = factory
)

internal class KbUiViewModelFactory<T : ViewModel>(
    private val createInstance: (KbUiComponent) -> T
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        createInstance(KbUiComponent.require()) as T
}