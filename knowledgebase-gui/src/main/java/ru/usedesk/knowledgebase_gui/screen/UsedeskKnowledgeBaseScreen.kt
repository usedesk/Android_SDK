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
        private const val WITH_SUPPORT_BUTTON_KEY = "a"
        private const val WITH_ARTICLE_RATING_KEY = "b"
        private const val KNOWLEDGE_BASE_CONFIGURATION = "c"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            withSupportButton: Boolean = true,
            withArticleRating: Boolean = true,
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
        ): UsedeskKnowledgeBaseScreen = UsedeskKnowledgeBaseScreen().apply {
            arguments = createBundle(
                withSupportButton,
                withArticleRating,
                knowledgeBaseConfiguration
            )
        }

        @JvmStatic
        @JvmOverloads
        fun createBundle(
            withSupportButton: Boolean = true,
            withArticleRating: Boolean = true,
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
        ): Bundle = Bundle().apply {
            putBoolean(WITH_SUPPORT_BUTTON_KEY, withSupportButton)
            putBoolean(WITH_ARTICLE_RATING_KEY, withArticleRating)
            putParcelable(KNOWLEDGE_BASE_CONFIGURATION, knowledgeBaseConfiguration)
        }
    }
}