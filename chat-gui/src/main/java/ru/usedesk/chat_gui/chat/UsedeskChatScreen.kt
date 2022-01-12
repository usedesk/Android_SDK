package ru.usedesk.chat_gui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.usedesk.chat_gui.IUsedeskOnAttachmentClickListener
import ru.usedesk.chat_gui.IUsedeskOnClientTokenListener
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesPage
import ru.usedesk.chat_gui.chat.offlineform.IOnGoToChatListener
import ru.usedesk.chat_gui.chat.offlineform.IOnOfflineFormSelectorClick
import ru.usedesk.chat_gui.chat.offlineformselector.IItemSelectChangeListener
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.UsedeskToolbarAdapter
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_sdk.utils.getFromJson
import ru.usedesk.common_sdk.utils.putAsJson

class UsedeskChatScreen : UsedeskFragment(),
    IUsedeskOnAttachmentClickListener,
    IOnOfflineFormSelectorClick,
    IItemSelectChangeListener,
    IOnGoToChatListener {

    private val viewModel: ChatViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var attachmentDialog: UsedeskAttachmentDialog

    private lateinit var toolbarAdapter: UsedeskToolbarAdapter
    private lateinit var chatNavigation: ChatNavigation

    internal val mediaPlayerAdapter: MediaPlayerAdapter by lazy {
        MediaPlayerAdapter(
            this,
            playerViewModel
        )
    }

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

        toolbarAdapter = UsedeskToolbarAdapter(binding.toolbar).apply {
            setBackButton {
                requireActivity().onBackPressed()
            }
        }

        val agentName = argsGetString(AGENT_NAME_KEY)
        val rejectedFileExtensions = argsGetStringArray(REJECTED_FILE_EXTENSIONS_KEY, arrayOf())
        val configuration = arguments?.getFromJson(
            CHAT_CONFIGURATION_KEY,
            UsedeskChatConfiguration::class.java
        )
        if (configuration != null) {
            UsedeskChatSdk.setConfiguration(configuration)
        }

        init(agentName, rejectedFileExtensions, savedInstanceState != null)

        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachmentDialog = UsedeskAttachmentDialog.create(this)
    }

    private fun init(
        agentName: String?,
        rejectedFileExtensions: Array<String>,
        inited: Boolean
    ) {
        UsedeskChatSdk.init(requireContext())

        ChatNavigation(childFragmentManager, binding.rootView, R.id.page_container).let {
            chatNavigation = it
            viewModel.init(it, agentName, rejectedFileExtensions, inited)
        }

        viewModel.exceptionLiveData.observe(viewLifecycleOwner) {
            it?.let {
                onException(it)
            }
        }

        viewModel.pageLiveData.observe(viewLifecycleOwner) {
            it?.let { page ->
                val title = when (page) {
                    ChatNavigation.Page.LOADING,
                    ChatNavigation.Page.MESSAGES -> {
                        binding.styleValues
                            .getStyleValues(R.attr.usedesk_common_toolbar)
                            .getStyleValues(R.attr.usedesk_common_toolbar_title_text)
                            .getString(android.R.attr.text)
                    }
                    ChatNavigation.Page.OFFLINE_FORM -> {
                        viewModel.offlineFormSettings?.callbackTitle
                    }
                    ChatNavigation.Page.OFFLINE_FORM_SELECTOR -> {
                        viewModel.offlineFormSettings?.topicsTitle
                    }
                }
                toolbarAdapter.setTitle(title)
            }
        }

        viewModel.clientTokenLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                getParentListener<IUsedeskOnClientTokenListener>()?.onClientToken(it)
            }
        }
    }

    override fun onAttachmentClick() {
        getParentListener<IUsedeskOnAttachmentClickListener>()?.onAttachmentClick()
            ?: attachmentDialog.show()
    }

    override fun onItemSelectChange(index: Int) {
        viewModel.setSubjectIndex(index)
    }

    private fun onException(exception: Exception) {
        exception.printStackTrace()
    }

    override fun onPause() {
        super.onPause()

        mediaPlayerAdapter.onPause()
    }

    override fun onResume() {
        super.onResume()

        mediaPlayerAdapter.onResume()

        val uriList = attachmentDialog.getAttachedUri(true)
        if (uriList.isNotEmpty()) {
            getCurrentFragment()?.let {
                if (it is MessagesPage) {
                    it.setAttachedFiles(uriList.map { uri ->
                        UsedeskFileInfo.create(
                            requireContext(),
                            uri
                        )
                    })
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        UsedeskChatSdk.stopService(requireContext())
    }

    override fun onStop() {
        super.onStop()
        UsedeskChatSdk.startService(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mediaPlayerAdapter.release()
    }

    override fun onBackPressed(): Boolean {
        return mediaPlayerAdapter.onBackPressed() || viewModel.onBackPressed()
    }

    private fun getCurrentFragment(): Fragment? {
        return childFragmentManager.findFragmentById(R.id.page_container)
    }

    override fun onOfflineFormSelectorClick(items: List<String>, selectedIndex: Int) {
        viewModel.goOfflineFormSelector(items.toTypedArray(), selectedIndex)
    }

    override fun onGoToMessages() {
        viewModel.goMessages()
    }

    companion object {
        private const val AGENT_NAME_KEY = "agentNameKey"
        private const val REJECTED_FILE_EXTENSIONS_KEY = "rejectedFileExtensionsKey"
        private const val CHAT_CONFIGURATION_KEY = "chatConfigurationKey"

        @JvmOverloads
        @JvmStatic
        fun newInstance(
            agentName: String? = null,
            rejectedFileExtensions: Collection<String>? = null,
            usedeskChatConfiguration: UsedeskChatConfiguration? = null
        ): UsedeskChatScreen {
            return UsedeskChatScreen().apply {
                arguments = Bundle().apply {
                    if (agentName != null) {
                        putString(AGENT_NAME_KEY, agentName)
                    }
                    val extensions = rejectedFileExtensions?.map {
                        '.' + it.trim(' ', '.')
                    }?.toTypedArray() ?: arrayOf()
                    if (usedeskChatConfiguration != null) {
                        putAsJson(CHAT_CONFIGURATION_KEY, usedeskChatConfiguration)
                    }
                    putStringArray(REJECTED_FILE_EXTENSIONS_KEY, extensions)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val toolbar =
            UsedeskToolbarAdapter.Binding(rootView.findViewById(R.id.toolbar), defaultStyleId)
    }
}