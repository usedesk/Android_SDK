package ru.usedesk.chat_gui.chat.messages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.chat_gui.UsedeskOnAttachmentClickListener
import ru.usedesk.chat_gui.UsedeskOnDownloadListener
import ru.usedesk.chat_gui.UsedeskOnFileClickListener
import ru.usedesk.chat_gui.UsedeskOnUrlClickListener
import ru.usedesk.chat_gui.chat.ChatArgs
import ru.usedesk.chat_gui.chat.UsedeskChatScreen
import ru.usedesk.chat_gui.chat.di.ChatUiComponent
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
import ru.usedesk.common_sdk.utils.UsedeskFileUtil.getFileSize
import ru.usedesk.chat_gui.R as chatR

internal class MessagesPage : UsedeskFragment() {

    private val viewModel: MessagesViewModel by viewModels(
        ownerProducer = this@MessagesPage::requireChatViewModelStoreOwner,
        factoryProducer = { ChatUiComponent.open(requireContext()).viewModelFactory }
    )

    private lateinit var binding: Binding

    private var messagesAdapter: MessagesAdapter? = null
    private var attachmentDialog: AttachmentDialog? = null
    private var formSelectorDialog: FormSelectorDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateItem(
        inflater = inflater,
        container = container,
        defaultLayoutId = chatR.layout.usedesk_page_messages,
        defaultStyleId = chatR.style.Usedesk_Chat_Screen_Messages_Page,
        createBinding = ::Binding,
    ).also {
        binding = it
    }.rootView

    fun dismissAnyDialog() {
        attachmentDialog?.dismiss()
        formSelectorDialog?.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findParent<UsedeskChatScreen>()?.run {
            val chatArgs = getChatArgs(savedInstanceState)

            init(
                chatArgs = chatArgs,
                savedInstanceState = savedInstanceState,
            )
        }

        attachmentDialog = AttachmentDialog.create(this).apply {
            setOnDismissListener {
                viewModel.onEvent(MessagesViewModel.Event.ShowAttachmentPanel(false))
            }
        }

        formSelectorDialog = FormSelectorDialog.create(this)

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
            useCameraFile { cameraFile ->
                if (it) {
                    val file = UsedeskFileInfo.create(
                        requireContext(),
                        cameraFile.toUri()
                    )
                    attachFiles(setOf(file))
                }
            }
        }

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.formSelector != new.formSelector) {
                when (new.formSelector) {
                    null -> formSelectorDialog?.dismiss()
                    else -> formSelectorDialog?.run {
                        update(
                            new.formSelector,
                            onSelected = { selected ->
                                viewModel.onEvent(
                                    MessagesViewModel.Event.FormChanged(
                                        new.formSelector.formId,
                                        new.formSelector.list.copy(selected = selected)
                                    )
                                )
                            }
                        )
                        show()
                    }
                }
            }
            if (old?.attachmentPanelVisible != new.attachmentPanelVisible) {
                when {
                    new.attachmentPanelVisible -> attachmentDialog?.show()
                    else -> attachmentDialog?.dismiss()
                }
            }
            if (old?.openUrl != new.openUrl) {
                new.openUrl?.use {
                    findParent<UsedeskOnUrlClickListener>()?.onUrlClick(it) ?: onUrlClick(it)
                }
            }
        }
    }

    private fun attachFiles(files: Set<UsedeskFileInfo>) {
        val filteredFiles = files.filter { fileInfo ->
            val size = requireContext().getFileSize(fileInfo.uri)
            return@filter size in 0..MAX_FILE_SIZE
        }.toSet()

        val rejectedFiles = files - filteredFiles
        if (rejectedFiles.isNotEmpty()) {
            val message = getString(
                when (rejectedFiles.size) {
                    1 -> chatR.string.usedesk_file_size_exceeds
                    else -> chatR.string.usedesk_files_size_exceeds
                },
                MAX_FILE_SIZE_MB
            )
            val toastText = "$message\n" + rejectedFiles.joinToString(
                separator = "\n",
                transform = UsedeskFileInfo::name
            )
            Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show()
        }
        viewModel.onEvent(MessagesViewModel.Event.AttachFiles(filteredFiles))
    }

    override fun onDestroyView() {
        super.onDestroyView()

        messagesAdapter = null

        attachmentDialog?.run {
            setOnDismissListener(null)
            dismiss()
        }
        attachmentDialog = null

        formSelectorDialog?.run {
            setOnDismissListener(null)
            dismiss()
        }
        formSelectorDialog = null
    }

    private fun init(
        chatArgs: ChatArgs,
        savedInstanceState: Bundle?,
    ) {
        viewModel.onEvent(MessagesViewModel.Event.Init(chatArgs.groupAgentMessages))
        UsedeskChatSdk.init(requireContext())

        MessagePanelAdapter(
            binding = binding.messagePanel,
            viewModel = viewModel,
            supportWindowInsets = chatArgs.supportWindowInsets,
            lifecycleCoroutineScope = lifecycleScope,
        ) {
            findParent<UsedeskOnAttachmentClickListener>()?.onAttachmentClick()
                ?: viewModel.onEvent(MessagesViewModel.Event.ShowAttachmentPanel(true))
        }

        val mediaPlayerAdapter = findParent<UsedeskChatScreen>()!!.mediaPlayerAdapter

        messagesAdapter = MessagesAdapter(
            recyclerView = binding.rvMessages,
            dateBinding = binding.dateBinding,
            viewModel = viewModel,
            lifecycleScope = lifecycleScope,
            chatArgs = chatArgs,
            mediaPlayerAdapter = mediaPlayerAdapter,
            onFileClick = { findParent<UsedeskOnFileClickListener>()?.onFileClick(it) },
            onFileDownloadClick = {
                findParent<UsedeskOnDownloadListener>()?.onDownload(
                    it.content,
                    it.name
                )
            },
            savedStated = savedInstanceState
        )

        FabToBottomAdapter(
            fabContainer = binding.fabContainer,
            fabToBottom = binding.fabToBottom,
            tvToBottom = binding.tvToBottom,
            parentStyleValues = binding.styleValues,
            viewModel = viewModel,
            lifecycleCoroutineScope = lifecycleScope,
            onClickListener = { messagesAdapter?.scrollToBottom() }
        )
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
        val rvMessages: RecyclerView = rootView.findViewById(chatR.id.rv_messages)
        val fabContainer: ViewGroup = rootView.findViewById(chatR.id.fab_container)
        val fabToBottom: FloatingActionButton = rootView.findViewById(chatR.id.fab_to_bottom)
        val tvToBottom: TextView = rootView.findViewById(chatR.id.tv_to_bottom_counter)
        val messagePanel =
            MessagePanelAdapter.Binding(
                rootView.findViewById(chatR.id.l_message_panel),
                defaultStyleId
            )
        val lMessagesContainer: ViewGroup = rootView.findViewById(chatR.id.l_messages_container)
        val dateBinding = getDateBinding(lMessagesContainer)

        private fun getDateBinding(rootView: ViewGroup): DateBinding {
            val dateView = rootView.findViewWithTag<View>(DATE_ITEM_VIEW_TAG)
            return when {
                dateView != null -> DateBinding(dateView, chatR.style.Usedesk_Chat_Date)
                else -> inflateItem(
                    rootView,
                    chatR.layout.usedesk_item_chat_date,
                    chatR.style.Usedesk_Chat_Date
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