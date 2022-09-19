package ru.usedesk.chat_gui.chat.messages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.chat_gui.*
import ru.usedesk.chat_gui.chat.UsedeskChatScreen
import ru.usedesk.chat_gui.chat.messages.adapters.FabToBottomAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessagePanelAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessagesAdapter
import ru.usedesk.chat_gui.chat.requireChatViewModelStoreOwner
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.UsedeskChatSdk.MAX_FILE_SIZE
import ru.usedesk.chat_sdk.UsedeskChatSdk.MAX_FILE_SIZE_MB
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem

internal class MessagesPage : UsedeskFragment() {

    private val viewModel: MessagesViewModel by viewModels(
        ownerProducer = this@MessagesPage::requireChatViewModelStoreOwner
    )

    private lateinit var binding: Binding

    private var messagesAdapter: MessagesAdapter? = null

    private var attachmentDialog: UsedeskAttachmentDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateItem(
        inflater,
        container,
        R.layout.usedesk_page_messages,
        R.style.Usedesk_Chat_Screen_Messages_Page,
        ::Binding
    ).also {
        binding = it
    }.rootView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findParent<UsedeskChatScreen>()?.getBundleArgs { _,
            agentName,
            rejectedFileExtensions,
            messagesDateFormat,
            messageTimeFormat,
            adaptiveTextMessageTimePadding,
            groupAgentMessages ->
            init(
                agentName,
                rejectedFileExtensions ?: arrayOf(),
                savedInstanceState,
                messagesDateFormat,
                messageTimeFormat,
                adaptiveTextMessageTimePadding,
                groupAgentMessages
            )
        }

        attachmentDialog = UsedeskAttachmentDialog.create(this).apply {
            setOnDismissListener { viewModel.showAttachmentPanel(false) }
        }

        registerFiles { uris ->
            val files = uris.map {
                UsedeskFileInfo.create(
                    requireContext(),
                    it
                )
            }.toSet()
            attachFiles(files)
        }
        registerCamera {
            viewModel.useCameraFile { cameraFile ->
                if (it) {
                    val file = UsedeskFileInfo.create(
                        requireContext(),
                        Uri.fromFile(cameraFile)
                    )
                    attachFiles(setOf(file))
                }
            }
        }
        registerCameraPermission {
            val cameraFile = generateCameraFile()
            viewModel.setCameraFile(cameraFile)
            startCamera(cameraFile)
        }

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.attachmentPanelVisible != new.attachmentPanelVisible) {
                if (new.attachmentPanelVisible) {
                    attachmentDialog?.show()
                }
            }
        }
    }

    private fun attachFiles(files: Set<UsedeskFileInfo>) {
        val filteredFiles = files.filter { fileInfo ->
            try {
                var size = -1L
                when (fileInfo.uri.scheme) {
                    "content" -> requireContext().contentResolver.query(
                        fileInfo.uri,
                        null,
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)
                            size = cursor.getLong(columnIndex)
                        }
                    }
                    "file" -> requireContext().contentResolver.openAssetFileDescriptor(
                        fileInfo.uri,
                        "r"
                    )?.use {
                        size = it.length
                    }
                }
                return@filter size in 0..MAX_FILE_SIZE
            } catch (e: Exception) {
                e.printStackTrace()
            }
            false
        }.toSet()

        val rejectedFiles = files - filteredFiles
        if (rejectedFiles.isNotEmpty()) {
            val message = getString(
                when (rejectedFiles.size) {
                    1 -> R.string.usedesk_file_size_exceeds
                    else -> R.string.usedesk_files_size_exceeds
                },
                MAX_FILE_SIZE_MB
            )
            val toastText = "$message\n" + rejectedFiles.joinToString(separator = "\n") { it.name }
            Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show()
        }
        viewModel.attachFiles(filteredFiles)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        messagesAdapter = null

        attachmentDialog?.run {
            setOnDismissListener(null)
            dismiss()
        }
        attachmentDialog = null
    }

    private fun init(
        agentName: String?,
        rejectedFileExtensions: Array<String>,
        savedInstanceState: Bundle?,
        messagesDateFormat: String,
        messageTimeFormat: String,
        adaptiveTextMessageTimePadding: Boolean,
        groupAgentMessages: Boolean
    ) {
        viewModel.groupAgentMessages = groupAgentMessages
        UsedeskChatSdk.init(requireContext())

        MessagePanelAdapter(
            binding.messagePanel,
            viewModel,
            lifecycleScope
        ) {
            findParent<IUsedeskOnAttachmentClickListener>()?.onAttachmentClick()
                ?: viewModel.showAttachmentPanel(true)
        }

        val mediaPlayerAdapter = findParent<UsedeskChatScreen>()!!.mediaPlayerAdapter

        messagesAdapter = MessagesAdapter(
            binding.rvMessages,
            binding.dateBinding,
            viewModel,
            lifecycleScope,
            agentName,
            rejectedFileExtensions,
            mediaPlayerAdapter,
            { findParent<IUsedeskOnFileClickListener>()?.onFileClick(it) },
            { findParent<IUsedeskOnUrlClickListener>()?.onUrlClick(it) ?: onUrlClick(it) },
            { findParent<IUsedeskOnDownloadListener>()?.onDownload(it.content, it.name) },
            messagesDateFormat,
            messageTimeFormat,
            adaptiveTextMessageTimePadding,
            savedInstanceState
        )

        FabToBottomAdapter(
            binding.fabContainer,
            binding.fabToBottom,
            binding.tvToBottom,
            binding.styleValues,
            viewModel,
            lifecycleScope
        ) { messagesAdapter?.scrollToBottom() }
    }

    private fun onUrlClick(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val rvMessages: RecyclerView = rootView.findViewById(R.id.rv_messages)
        val fabContainer: ViewGroup = rootView.findViewById(R.id.fab_container)
        val fabToBottom: FloatingActionButton = rootView.findViewById(R.id.fab_to_bottom)
        val tvToBottom: TextView = rootView.findViewById(R.id.tv_to_bottom_counter)
        val messagePanel =
            MessagePanelAdapter.Binding(rootView.findViewById(R.id.l_message_panel), defaultStyleId)
        val lMessagesContainer: ViewGroup = rootView.findViewById(R.id.l_messages_container)
        val dateBinding = getDateBinding(lMessagesContainer)

        private fun getDateBinding(rootView: ViewGroup): DateBinding {
            val dateView = rootView.findViewWithTag<View>(DATE_ITEM_VIEW_TAG)
            return when {
                dateView != null -> DateBinding(dateView, R.style.Usedesk_Chat_Date)
                else -> inflateItem(
                    rootView,
                    R.layout.usedesk_item_chat_date,
                    R.style.Usedesk_Chat_Date
                ) { view, style ->
                    view.tag = DATE_ITEM_VIEW_TAG
                    rootView.addView(view)
                    view.visibility = View.INVISIBLE
                    DateBinding(view, style)
                }
            }
        }
    }

    companion object {
        private const val DATE_ITEM_VIEW_TAG = "dateItemTag"
    }
}