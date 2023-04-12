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
                argsGetParcelable<UsedeskKnowledgeBaseConfiguration>(KEY_CONFIGURATION)
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
        val isSupportButtonVisible = arguments?.getBoolean(KEY_WITH_SUPPORT_BUTTON) ?: true
        setContent {
            val theme = remember { UsedeskKnowledgeBaseTheme.provider() }
            ComposeRoot(
                theme = theme,
                isSupportButtonVisible = isSupportButtonVisible,
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
        private const val KEY_CONFIGURATION = "a"
        private const val KEY_WITH_SUPPORT_BUTTON = "b"
        private const val KEY_SECTION_ID = "c"
        private const val KEY_CATEGORY_ID = "d"
        private const val KEY_ARTICLE_ID = "e"

        @JvmStatic
        fun newInstance(
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration,
            withSupportButton: Boolean = true,
            sectionId: Long? = null,
            categoryId: Long? = null,
            articleId: Long? = null
        ): UsedeskKnowledgeBaseScreen = UsedeskKnowledgeBaseScreen().apply {
            arguments = createBundle(
                knowledgeBaseConfiguration,
                withSupportButton,
                sectionId,
                categoryId,
                articleId
            )
        }

        @JvmStatic
        fun createBundle(
            configuration: UsedeskKnowledgeBaseConfiguration,
            withSupportButton: Boolean = true,
            sectionId: Long? = null,
            categoryId: Long? = null,
            articleId: Long? = null
        ): Bundle = Bundle().apply {
            putParcelable(KEY_CONFIGURATION, configuration)
            putBoolean(KEY_WITH_SUPPORT_BUTTON, withSupportButton)
            if (sectionId != null) {
                putLong(KEY_SECTION_ID, sectionId)
            }
            if (categoryId != null) {
                putLong(KEY_CATEGORY_ID, categoryId)
            }
            if (articleId != null) {
                putLong(KEY_ARTICLE_ID, articleId)
            }
        }
    }
}