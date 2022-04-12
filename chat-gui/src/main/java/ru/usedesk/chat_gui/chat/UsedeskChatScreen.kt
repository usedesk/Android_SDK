package ru.usedesk.chat_gui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import ru.usedesk.chat_gui.IUsedeskOnClientTokenListener
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.UsedeskToolbarAdapter
import ru.usedesk.common_gui.inflateItem

class UsedeskChatScreen : UsedeskFragment() {

    private val viewModel: ChatViewModel by viewModels(
        ownerProducer = {
            findChatViewModelStoreOwner() ?: this
        }
    )
    private val playerViewModel: PlayerViewModel by viewModels(
        ownerProducer = {
            findChatViewModelStoreOwner() ?: this
        })

    internal val mediaPlayerAdapter: MediaPlayerAdapter by lazy {
        MediaPlayerAdapter(
            this,
            playerViewModel
        )
    }

    private lateinit var binding: Binding
    private lateinit var toolbarAdapter: UsedeskToolbarAdapter
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_screen_chat,
            R.style.Usedesk_Chat_Screen
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        navHostFragment =
            childFragmentManager.findFragmentById(R.id.page_container) as NavHostFragment
        navController = navHostFragment.navController

        toolbarAdapter = UsedeskToolbarAdapter(binding.toolbar).apply {
            setBackButton {
                requireActivity().onBackPressed()
            }
        }

        getBundleArgs { chatConfiguration, _, _, _, _, _, _ ->
            if (chatConfiguration != null) {
                UsedeskChatSdk.setConfiguration(chatConfiguration)
            }
            init()
        }

        return binding.rootView
    }

    internal fun getBundleArgs(
        onArgs: (
            UsedeskChatConfiguration?,
            String?,
            Array<String>?,
            String,
            String,
            Boolean,
            Boolean
        ) -> Unit
    ) {
        onArgs(
            argsGetParcelable(CHAT_CONFIGURATION_KEY),
            argsGetString(AGENT_NAME_KEY),
            argsGetStringArray(REJECTED_FILE_EXTENSIONS_KEY),
            argsGetString(MESSAGES_DATE_FORMAT_KEY, MESSAGES_DATE_FORMAT_DEFAULT),
            argsGetString(MESSAGE_TIME_FORMAT_KEY, MESSAGE_TIME_FORMAT_DEFAULT),
            argsGetBoolean(ADAPTIVE_TEXT_MESSAGE_TIME_PADDING_KEY, false),
            argsGetBoolean(GROUP_AGENT_MESSAGES, true)
        )
    }

    private fun init() {
        UsedeskChatSdk.init(requireContext())

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val title = when (destination.id) {
                R.id.dest_loading_page,
                R.id.dest_messages_page -> {
                    binding.styleValues
                        .getStyleValues(R.attr.usedesk_common_toolbar)
                        .getStyleValues(R.attr.usedesk_common_toolbar_title_text)
                        .getString(android.R.attr.text)
                }
                R.id.dest_offline_form_page -> {
                    viewModel.modelLiveData.value.offlineFormSettings?.callbackTitle
                }
                R.id.dest_offline_form_selector_page -> {
                    viewModel.modelLiveData.value.offlineFormSettings?.topicsTitle
                }
                else -> null
            }
            toolbarAdapter.setTitle(title)
        }
        viewModel.modelLiveData.initAndObserveWithOld(viewLifecycleOwner) { old, new ->
            if (old != null &&
                new.clientToken != null &&
                old.clientToken != new.clientToken
            ) {
                findParent<IUsedeskOnClientTokenListener>()?.onClientToken(new.clientToken)
            }
            if (old?.offlineFormSettings != new.offlineFormSettings) {
                updateTitle(navController.currentDestination)
            }
        }

        viewModel.init()

        viewModel.goLoadingEvent.process {
            navController.navigate(R.id.dest_loading_page)
        }
    }

    private fun updateTitle(destination: NavDestination?) {
        val title = when (destination?.id) {
            R.id.dest_loading_page,
            R.id.dest_messages_page -> {
                binding.styleValues
                    .getStyleValues(R.attr.usedesk_common_toolbar)
                    .getStyleValues(R.attr.usedesk_common_toolbar_title_text)
                    .getString(android.R.attr.text)
            }
            R.id.dest_offline_form_page -> {
                viewModel.modelLiveData.value.offlineFormSettings?.callbackTitle
            }
            R.id.dest_offline_form_selector_page -> {
                viewModel.modelLiveData.value.offlineFormSettings?.topicsTitle
            }
            else -> null
        }
        toolbarAdapter.setTitle(title)
    }

    override fun onStart() {
        super.onStart()

        UsedeskChatSdk.stopService(requireContext())
    }

    override fun onStop() {
        super.onStop()

        UsedeskChatSdk.startService(requireContext())
    }

    override fun onBackPressed(): Boolean {
        return mediaPlayerAdapter.onBackPressed() || navController.popBackStack()
    }

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
            agentName: String? = null,
            rejectedFileExtensions: Collection<String>? = null,
            usedeskChatConfiguration: UsedeskChatConfiguration? = null,
            messagesDateFormat: String? = null,
            messageTimeFormat: String? = null,
            adaptiveTextMessageTimePadding: Boolean = false,
            groupAgentMessages: Boolean = true
        ): UsedeskChatScreen {
            return UsedeskChatScreen().apply {
                arguments = createBundle(
                    agentName,
                    rejectedFileExtensions,
                    usedeskChatConfiguration,
                    messagesDateFormat,
                    messageTimeFormat,
                    adaptiveTextMessageTimePadding
                )
            }
        }

        @JvmOverloads
        @JvmStatic
        fun createBundle(
            agentName: String? = null,
            rejectedFileExtensions: Collection<String>? = null,
            usedeskChatConfiguration: UsedeskChatConfiguration? = null,
            messagesDateFormat: String? = null,
            messageTimeFormat: String? = null,
            adaptiveTextMessageTimePadding: Boolean = false,
            groupAgentMessages: Boolean = true
        ): Bundle {
            return Bundle().apply {
                if (agentName != null) {
                    putString(AGENT_NAME_KEY, agentName)
                }
                val extensions = rejectedFileExtensions?.map {
                    '.' + it.trim(' ', '.')
                }?.toTypedArray() ?: arrayOf()
                if (usedeskChatConfiguration != null) {
                    putParcelable(CHAT_CONFIGURATION_KEY, usedeskChatConfiguration)
                }
                putStringArray(REJECTED_FILE_EXTENSIONS_KEY, extensions)
                putString(MESSAGES_DATE_FORMAT_KEY, messagesDateFormat)
                putString(MESSAGE_TIME_FORMAT_KEY, messageTimeFormat)
                putBoolean(ADAPTIVE_TEXT_MESSAGE_TIME_PADDING_KEY, adaptiveTextMessageTimePadding)
                putBoolean(GROUP_AGENT_MESSAGES, groupAgentMessages)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val toolbar =
            UsedeskToolbarAdapter.Binding(rootView.findViewById(R.id.toolbar), defaultStyleId)
    }
}