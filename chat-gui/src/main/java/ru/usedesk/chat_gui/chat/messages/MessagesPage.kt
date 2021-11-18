package ru.usedesk.chat_gui.chat.messages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.chat_gui.*
import ru.usedesk.chat_gui.chat.messages.adapters.FabToBottomAdapter
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

    private lateinit var messagesAdapter: MessagesAdapter

    private var cleared = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_page_messages,
            R.style.Usedesk_Chat_Screen_Messages_Page
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        val agentName: String? = argsGetString(AGENT_NAME_KEY)
        val rejectedFileExtensions = argsGetStringArray(REJECTED_FILE_EXTENSIONS_KEY, arrayOf())

        init(agentName, rejectedFileExtensions)

        return binding.rootView
    }

    private fun init(agentName: String?, rejectedFileExtensions: Array<String>) {
        UsedeskChatSdk.init(requireContext())

        MessagePanelAdapter(
            binding.messagePanel,
            viewModel,
            viewLifecycleOwner
        ) {
            getParentListener<IUsedeskOnAttachmentClickListener>()?.onAttachmentClick()
        }

        val mediaPlayerAdapter =
            getParentListener<IUsedeskMediaPlayerAdapterKeeper>()?.getMediaPlayerAdapter()
                ?: throw RuntimeException("Parent must implement IUsedeskMediaPlayerAdapterKeeper")

        messagesAdapter = MessagesAdapter(
            binding.rvMessages,
            viewModel,
            viewLifecycleOwner,
            agentName,
            rejectedFileExtensions,
            mediaPlayerAdapter,
            {
                getParentListener<IUsedeskOnFileClickListener>()?.onFileClick(it)
            },
            {
                getParentListener<IUsedeskOnUrlClickListener>()?.onUrlClick(it)
                    ?: onUrlClick(it)
            })
        FabToBottomAdapter(
            binding.fabToBottom,
            binding.styleValues,
            viewModel,
            viewLifecycleOwner
        ) {
            messagesAdapter.scrollToBottom()
        }
    }

    private fun onUrlClick(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun setAttachedFiles(attachedFiles: List<UsedeskFileInfo>) {
        viewModel.addAttachedFiles(attachedFiles)
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
        private const val REJECTED_FILE_EXTENSIONS_KEY = "rejectedFileExtensionsKey"

        fun newInstance(
            agentName: String?,
            rejectedFileExtensions: Array<String>
        ): MessagesPage {
            return MessagesPage().apply {
                arguments = Bundle().apply {
                    if (agentName != null) {
                        putString(AGENT_NAME_KEY, agentName)
                    }
                    putStringArray(REJECTED_FILE_EXTENSIONS_KEY, rejectedFileExtensions)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val rvMessages: RecyclerView = rootView.findViewById(R.id.rv_messages)
        val fabToBottom: FloatingActionButton = rootView.findViewById(R.id.fab_to_bottom)
        val messagePanel =
            MessagePanelAdapter.Binding(rootView.findViewById(R.id.l_message_panel), defaultStyleId)
    }

    private class FileInfo(
        val uri: String,
        val type: String,
        val name: String
    ) {
        constructor(usedeskFileInfo: UsedeskFileInfo) : this(
            usedeskFileInfo.uri.toString(),
            usedeskFileInfo.type,
            usedeskFileInfo.name
        )

        fun getUsedeskFileInfo(): UsedeskFileInfo = UsedeskFileInfo(Uri.parse(uri), type, name)
    }
}