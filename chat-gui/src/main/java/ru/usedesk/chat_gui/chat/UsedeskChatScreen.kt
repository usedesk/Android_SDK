package ru.usedesk.chat_gui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.usedesk.chat_gui.IUsedeskOnAttachmentClickListener
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

class UsedeskChatScreen : UsedeskFragment(),
    IUsedeskOnAttachmentClickListener,
    IOnOfflineFormSelectorClick,
    IItemSelectChangeListener,
    IOnGoToChatListener {

    private val viewModel: ChatViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var attachment: UsedeskAttachmentDialog

    private lateinit var toolbarAdapter: UsedeskToolbarAdapter
    private var chatNavigation: ChatNavigation? = null

    private var cleared = false

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

        toolbarAdapter =
            UsedeskToolbarAdapter(requireActivity() as AppCompatActivity, binding.toolbar).apply {
                setBackButton {
                    requireActivity().onBackPressed()
                }
            }

        val agentName = argsGetString(AGENT_NAME_KEY)
        val rejectedFileExtensions = argsGetStringArray(REJECTED_FILE_EXTENSIONS_KEY, arrayOf())
        val configurationJson = argsGetString(CHAT_CONFIGURATION_KEY)
        val configuration = UsedeskChatConfiguration.fromJson(configurationJson)
        if (configuration != null) {
            UsedeskChatSdk.setConfiguration(configuration)
        }

        init(agentName, rejectedFileExtensions)

        chatNavigation?.fragmentManager = childFragmentManager

        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachment = UsedeskAttachmentDialog.create(this)
    }

    private fun init(agentName: String?, rejectedFileExtensions: Array<String>) {
        UsedeskChatSdk.init(requireContext())

        if (chatNavigation == null) {
            ChatNavigation(childFragmentManager, binding.rootView, R.id.page_container).let {
                chatNavigation = it
                viewModel.init(it, agentName, rejectedFileExtensions)
            }
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
                        binding.styleValues.getStyleValues(R.attr.usedesk_common_toolbar_title_text)
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
    }

    override fun onAttachmentClick() {
        getParentListener<IUsedeskOnAttachmentClickListener>()?.onAttachmentClick()
            ?: attachment.show()
    }

    override fun onItemSelectChange(index: Int) {
        viewModel.setSubjectIndex(index)
    }

    private fun onException(exception: Exception) {
        exception.printStackTrace()
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
        return viewModel.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        attachment.onActivityResult(requestCode, resultCode, data)
    }

    fun setAttachedFiles(attachedFiles: List<UsedeskFileInfo>) {
        getCurrentFragment()?.let {
            if (it is MessagesPage) {
                it.setAttachedFiles(attachedFiles)
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()

        if (cleared) {
            getCurrentFragment()?.let {
                if (it is MessagesPage) {
                    it.clear()
                }
            }
        }
    }

    fun clear() {
        cleared = true
    }

    companion object {
        private const val AGENT_NAME_KEY = "71bfed73"
        private const val REJECTED_FILE_EXTENSIONS_KEY = "22a84bb9"
        private const val CHAT_CONFIGURATION_KEY = "a5ed81be"

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
                        val jsonConfiguration = usedeskChatConfiguration.toJson()
                        putString(CHAT_CONFIGURATION_KEY, jsonConfiguration)
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