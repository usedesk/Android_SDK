
package ru.usedesk.knowledgebase_gui.screen

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import kotlinx.parcelize.Parcelize
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
            val deepLink = argsGetParcelable<DeepLink>(KEY_DEEP_LINK)
            KbUiViewModelFactory {
                RootViewModel(
                    it.interactor,
                    deepLink
                )
            }
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
        private const val KEY_DEEP_LINK = "c"

        @JvmStatic
        fun newInstance(
            configuration: UsedeskKnowledgeBaseConfiguration,
            withSupportButton: Boolean = true,
            deepLink: DeepLink? = null
        ): UsedeskKnowledgeBaseScreen = UsedeskKnowledgeBaseScreen().apply {
            arguments = createBundle(
                configuration,
                withSupportButton,
                deepLink
            )
        }

        @JvmStatic
        fun createBundle(
            configuration: UsedeskKnowledgeBaseConfiguration,
            withSupportButton: Boolean = true,
            deepLink: DeepLink? = null
        ): Bundle = Bundle().apply {
            putParcelable(KEY_CONFIGURATION, configuration)
            putBoolean(KEY_WITH_SUPPORT_BUTTON, withSupportButton)
            if (deepLink != null) {
                putParcelable(KEY_DEEP_LINK, deepLink)
            }
        }
    }

    sealed interface DeepLink : Parcelable {
        val noBackStack: Boolean

        @Parcelize
        data class Section(
            val sectionId: Long,
            override val noBackStack: Boolean
        ) : DeepLink

        @Parcelize
        data class Category(
            val categoryId: Long,
            override val noBackStack: Boolean
        ) : DeepLink

        @Parcelize
        data class Article(
            val articleId: Long,
            override val noBackStack: Boolean
        ) : DeepLink
    }
}