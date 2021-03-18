package ru.usedesk.chat_gui.chat.messages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.IUsedeskOnAttachmentClickListener
import ru.usedesk.chat_gui.IUsedeskOnFileClickListener
import ru.usedesk.chat_gui.IUsedeskOnUrlClickListener
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.adapters.MessagePanelAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessagesAdapter
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem

internal class MessagesPage : UsedeskFragment() {

    private val viewModel: MessagesViewModel by viewModels()

    private lateinit var binding: Binding

    private lateinit var messagePanelAdapter: MessagePanelAdapter
    private lateinit var messagesAdapter: MessagesAdapter

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
                    R.layout.usedesk_page_messages,
                    R.style.Usedesk_Chat_Screen) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }

            val agentName: String? = argsGetString(AGENT_NAME_KEY)

            init(agentName)
        }

        onLiveData()

        return binding.rootView
    }

    private fun onLiveData() {
        messagePanelAdapter.onLiveData(viewModel, viewLifecycleOwner)
        messagesAdapter.onLiveData(viewModel, viewLifecycleOwner)
    }

    private fun init(agentName: String?) {
        UsedeskChatSdk.init(requireContext())

        messagePanelAdapter = MessagePanelAdapter(binding.messagePanel, viewModel) {

            getParentListener<IUsedeskOnAttachmentClickListener>()?.onAttachmentClick()
        }

        messagesAdapter = MessagesAdapter(viewModel,
                binding.rvMessages,
                agentName,
                { file ->
                    getParentListener<IUsedeskOnFileClickListener>()?.onFileClick(file)
                },
                { url ->
                    getParentListener<IUsedeskOnUrlClickListener>()?.onUrlClick(url)
                            ?: this.onUrlClick(url)
                })
    }

    private fun onUrlClick(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun setAttachedFiles(attachedFiles: List<UsedeskFileInfo>) {
        viewModel.setAttachedFiles(attachedFiles)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (cleared) {
            messagesAdapter.clear()
        }
    }

    fun clear() {
        cleared = true
    }

    companion object {
        private const val AGENT_NAME_KEY = "agentNameKey"

        @JvmOverloads
        @JvmStatic
        fun newInstance(agentName: String? = null): MessagesPage {
            return MessagesPage().apply {
                arguments = Bundle().apply {
                    if (agentName != null) {
                        putString(AGENT_NAME_KEY, agentName)
                    }
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val rvMessages: RecyclerView = rootView.findViewById(R.id.rv_messages)
        val messagePanel = MessagePanelAdapter.Binding(rootView.findViewById(R.id.l_message_panel), defaultStyleId)
    }
}