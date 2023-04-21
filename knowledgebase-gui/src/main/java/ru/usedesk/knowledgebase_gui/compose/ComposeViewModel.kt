
package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.usedesk.knowledgebase_gui._di.KbUiComponent

@Composable
internal inline fun <reified T : ViewModel> kbUiViewModel(
    key: String? = null,
    viewModelStoreOwner: ViewModelStoreOwner,
    crossinline createInstance: (KbUiComponent) -> T
): T = viewModel(
    modelClass = T::class.java,
    key = key,
    viewModelStoreOwner = viewModelStoreOwner,
    factory = KbUiViewModelFactory { createInstance(it) }
)

internal class KbUiViewModelFactory<T : ViewModel>(
    private val createInstance: (KbUiComponent) -> T
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        createInstance(KbUiComponent.require()) as T
}