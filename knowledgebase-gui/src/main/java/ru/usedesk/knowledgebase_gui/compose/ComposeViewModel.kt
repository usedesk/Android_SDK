package ru.usedesk.knowledgebase_gui.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase

@Composable
internal inline fun <reified T : ViewModel> composeViewModel(
    key: String? = null,
    crossinline viewModelInstanceCreator: (usedeskKb: IUsedeskKnowledgeBase) -> T
): T = viewModel(
    modelClass = T::class.java,
    key = key,
    factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            viewModelInstanceCreator(UsedeskKnowledgeBaseSdk.requireInstance()) as T
    }
)