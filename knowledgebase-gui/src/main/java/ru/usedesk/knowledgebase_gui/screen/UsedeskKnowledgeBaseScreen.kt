package ru.usedesk.knowledgebase_gui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.knowledgebase_gui._di.KbUiComponent
import ru.usedesk.knowledgebase_gui.compose.KbUiViewModelFactory
import ru.usedesk.knowledgebase_gui.screen.compose.ComposeRoot
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

class UsedeskKnowledgeBaseScreen : UsedeskFragment() {
    private val viewModel: RootViewModel by viewModels(
        factoryProducer = {
            val configuration =
                argsGetParcelable<UsedeskKnowledgeBaseConfiguration>(KNOWLEDGE_BASE_CONFIGURATION)
                    ?: throw RuntimeException("UsedeskKnowledgeBaseConfiguration not found. Call the newInstance or createBundle method and put the configuration inside")
            KbUiComponent.open(requireContext(), configuration)
            KbUiViewModelFactory { RootViewModel(it.interactor) }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            val theme = remember { UsedeskKnowledgeBaseTheme.provider() }
            ComposeRoot(
                theme = theme,
                viewModel = viewModel,
                onBackPressed = remember { requireActivity()::onBackPressed },
                onGoSupport = remember {
                    { findParent<IUsedeskOnSupportClickListener>()?.onSupportClick() }
                },
                onWebUrl = remember { { findParent<IUsedeskOnWebUrlListener>()?.onWebUrl(it) } }
            )
        }
    }

    override fun onBackPressed() = viewModel.onBackPressed()

    companion object {
        private const val KNOWLEDGE_BASE_CONFIGURATION = "c"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
        ): UsedeskKnowledgeBaseScreen = UsedeskKnowledgeBaseScreen().apply {
            arguments = createBundle(knowledgeBaseConfiguration)
        }

        @JvmStatic
        @JvmOverloads
        fun createBundle(
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
        ): Bundle = Bundle().apply {
            putParcelable(KNOWLEDGE_BASE_CONFIGURATION, knowledgeBaseConfiguration)
        }
    }
}