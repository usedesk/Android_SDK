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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        if (savedInstanceState == null) {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_screen_chat,
                    R.style.Usedesk_Chat_Screen) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }

            toolbarAdapter = UsedeskToolbarAdapter(requireActivity() as AppCompatActivity, binding.toolbar).apply {
                setBackButton {
                    requireActivity().onBackPressed()
                }
            }

            val agentName: String? = argsGetString(AGENT_NAME_KEY)

            init(agentName)
        }

        chatNavigation?.fragmentManager = childFragmentManager

        onLiveData()

        return binding.rootView
    }

    private fun onLiveData() {
        viewModel.exceptionLiveData.observe(viewLifecycleOwner) {
            onException(it)
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
                        viewModel.offlineFormSettingsLiveData.value?.callbackTitle
                    }
                    ChatNavigation.Page.OFFLINE_FORM_SELECTOR -> {
                        viewModel.offlineFormSettingsLiveData.value?.topicsTitle
                    }
                }
                toolbarAdapter.setTitle(title)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachment = UsedeskAttachmentDialog.create(this)
    }

    private fun init(agentName: String?) {
        UsedeskChatSdk.init(requireContext())

        if (chatNavigation == null) {
            ChatNavigation(childFragmentManager, binding.rootView, R.id.page_container).let {
                chatNavigation = it
                viewModel.init(it, agentName)
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
        private const val AGENT_NAME_KEY = "agentNameKey"

        @JvmOverloads
        @JvmStatic
        fun newInstance(agentName: String? = null): UsedeskChatScreen {
            return UsedeskChatScreen().apply {
                arguments = Bundle().apply {
                    if (agentName != null) {
                        putString(AGENT_NAME_KEY, agentName)
                    }
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val toolbar = UsedeskToolbarAdapter.Binding(rootView.findViewById(R.id.toolbar), defaultStyleId)
    }
}