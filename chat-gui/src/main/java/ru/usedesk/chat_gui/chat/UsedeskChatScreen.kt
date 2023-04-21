
package ru.usedesk.chat_gui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import ru.usedesk.chat_gui.IUsedeskOnChatInitedListener
import ru.usedesk.chat_gui.IUsedeskOnClientTokenListener
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.di.ChatUiComponent
import ru.usedesk.chat_gui.chat.messages.MessagesPage
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.UsedeskToolbarAdapter
import ru.usedesk.common_gui.inflateItem

class UsedeskChatScreen : UsedeskFragment() {

    private val viewModel: ChatViewModel by viewModels(
        ownerProducer = { findChatViewModelStoreOwner() ?: this },
        factoryProducer = { ChatUiComponent.open(requireContext()).viewModelFactory }
    )
    private val playerViewModel: PlayerViewModel by viewModels(
        ownerProducer = { findChatViewModelStoreOwner() ?: this }
    )

    internal val mediaPlayerAdapter: MediaPlayerAdapter by lazy {
        MediaPlayerAdapter(
            this,
            playerViewModel
        )
    }

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateItem(
        inflater,
        container,
        R.layout.usedesk_screen_chat,
        R.style.Usedesk_Chat_Screen,
        ::Binding
    ).apply {
        navHostFragment =
            childFragmentManager.findFragmentById(R.id.page_container) as NavHostFragment
        navController = navHostFragment.navController

        getBundleArgs(savedInstanceState) { chatConfiguration, _, _, _, _, _, _ ->
            init(chatConfiguration)
        }
    }.rootView

    fun dismissAnyDialog() {
        navHostFragment
            .childFragmentManager
            .fragments
            .filterIsInstance<MessagesPage>()
            .firstOrNull()
            ?.dismissAnyDialog()
    }

    internal fun getBundleArgs(
        savedInstanceState: Bundle?,
        onArgs: (
            UsedeskChatConfiguration,
            String?,
            Array<String>?,
            String,
            String,
            Boolean,
            Boolean
        ) -> Unit
    ) {
        onArgs(
            savedInstanceState?.getParcelable(CHAT_CONFIGURATION_KEY)
                ?: argsGetParcelable(CHAT_CONFIGURATION_KEY)
                ?: throw RuntimeException("UsedeskChatConfiguration not found. Call the newInstance or createBundle method and put the configuration inside"),
            argsGetString(AGENT_NAME_KEY),
            argsGetStringArray(REJECTED_FILE_EXTENSIONS_KEY),
            argsGetString(MESSAGES_DATE_FORMAT_KEY, MESSAGES_DATE_FORMAT_DEFAULT),
            argsGetString(MESSAGE_TIME_FORMAT_KEY, MESSAGE_TIME_FORMAT_DEFAULT),
            argsGetBoolean(ADAPTIVE_TEXT_MESSAGE_TIME_PADDING_KEY, false),
            argsGetBoolean(GROUP_AGENT_MESSAGES, true)
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        getBundleArgs(outState) { configuration, _, _, _, _, _, _ ->
            outState.putParcelable(
                CHAT_CONFIGURATION_KEY,
                configuration.copy(clientToken = viewModel.modelFlow.value.clientToken)
            )
        }
        super.onSaveInstanceState(outState)
    }

    private fun Binding.init(chatConfiguration: UsedeskChatConfiguration) {
        val usedeskChat = UsedeskChatSdk.init(
            requireContext(),
            chatConfiguration
        )
        findParent<IUsedeskOnChatInitedListener>()?.onChatInited(usedeskChat) //TODO: will it called single time?

        val toolbarAdapter = UsedeskToolbarAdapter(toolbar).apply {
            setBackButton(requireActivity()::onBackPressed)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            toolbarAdapter.updateTitle(styleValues, destination)
        }
        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old != null &&
                new.clientToken != null &&
                old.clientToken != new.clientToken
            ) {
                findParent<IUsedeskOnClientTokenListener>()?.onClientToken(new.clientToken)
            }
            if (old?.offlineFormSettings != new.offlineFormSettings) {
                toolbarAdapter.updateTitle(styleValues, navController.currentDestination)
            }
            if (old?.goLoading != new.goLoading) {
                new.goLoading.use {
                    while (navController.popBackStack()) continue
                    navController.navigate(R.id.dest_loadingPage)
                }
            }
        }
    }

    private fun UsedeskToolbarAdapter.updateTitle(
        styleValues: UsedeskResourceManager.StyleValues,
        destination: NavDestination?
    ) {
        val model = viewModel.modelFlow.value
        setTitle(
            when (destination?.id) {
                R.id.dest_loadingPage,
                R.id.dest_messagesPage -> styleValues
                    .getStyleValues(R.attr.usedesk_common_toolbar)
                    .getStyleValues(R.attr.usedesk_common_toolbar_title_text)
                    .getString(android.R.attr.text)
                R.id.dest_offlineFormPage -> model.offlineFormSettings?.callbackTitle
                R.id.dest_offlineFormSelectorPage -> model.offlineFormSettings?.topicsTitle
                else -> null
            }
        )
    }

    override fun onStart() {
        super.onStart()

        UsedeskChatSdk.stopService(requireContext())
    }

    override fun onStop() {
        super.onStop()

        UsedeskChatSdk.startService(requireContext())
    }

    override fun onBackPressed(): Boolean =
        mediaPlayerAdapter.onBackPressed() || navController.popBackStack()

    companion object {
        private const val AGENT_NAME_KEY = "agentNameKey"
        private const val REJECTED_FILE_EXTENSIONS_KEY = "rejectedFileExtensionsKey"
        private const val CHAT_CONFIGURATION_KEY = "chatConfigurationKey"
        private const val MESSAGES_DATE_FORMAT_KEY = "messagesDateFormatKey"
        private const val MESSAGE_TIME_FORMAT_KEY = "messageTimeFormatKey"
        private const val ADAPTIVE_TEXT_MESSAGE_TIME_PADDING_KEY =
            "adaptiveTextMessageTimePaddingKey"
        private const val GROUP_AGENT_MESSAGES = "groupAgentMessages"

        private const val MESSAGES_DATE_FORMAT_DEFAULT = "dd MMMM"
        private const val MESSAGE_TIME_FORMAT_DEFAULT = "HH:mm"

        @JvmOverloads
        @JvmStatic
        fun newInstance(
            usedeskChatConfiguration: UsedeskChatConfiguration,
            agentName: String? = null,
            rejectedFileExtensions: Collection<String>? = null,
            messagesDateFormat: String? = null,
            messageTimeFormat: String? = null,
            adaptiveTextMessageTimePadding: Boolean = false,
            groupAgentMessages: Boolean = true
        ): UsedeskChatScreen = UsedeskChatScreen().apply {
            arguments = createBundle(
                usedeskChatConfiguration,
                agentName,
                rejectedFileExtensions,
                messagesDateFormat,
                messageTimeFormat,
                adaptiveTextMessageTimePadding,
                groupAgentMessages
            )
        }

        @JvmOverloads
        @JvmStatic
        fun createBundle(
            usedeskChatConfiguration: UsedeskChatConfiguration,
            agentName: String? = null,
            rejectedFileExtensions: Collection<String>? = null,
            messagesDateFormat: String? = null,
            messageTimeFormat: String? = null,
            adaptiveTextMessageTimePadding: Boolean = false,
            groupAgentMessages: Boolean = true
        ): Bundle = Bundle().apply {
            putParcelable(CHAT_CONFIGURATION_KEY, usedeskChatConfiguration)
            if (agentName != null) {
                putString(AGENT_NAME_KEY, agentName)
            }
            val extensions = rejectedFileExtensions
                ?.map { '.' + it.trim(' ', '.') }
                ?.toTypedArray()
                ?: arrayOf()
            putStringArray(REJECTED_FILE_EXTENSIONS_KEY, extensions)
            putString(MESSAGES_DATE_FORMAT_KEY, messagesDateFormat)
            putString(MESSAGE_TIME_FORMAT_KEY, messageTimeFormat)
            putBoolean(ADAPTIVE_TEXT_MESSAGE_TIME_PADDING_KEY, adaptiveTextMessageTimePadding)
            putBoolean(GROUP_AGENT_MESSAGES, groupAgentMessages)
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val toolbar =
            UsedeskToolbarAdapter.Binding(rootView.findViewById(R.id.toolbar), defaultStyleId)
    }
}