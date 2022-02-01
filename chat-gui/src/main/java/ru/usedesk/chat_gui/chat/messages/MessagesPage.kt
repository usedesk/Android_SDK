package ru.usedesk.chat_gui.chat.messages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.chat_gui.*
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.chat_gui.chat.UsedeskChatScreen
import ru.usedesk.chat_gui.chat.messages.adapters.FabToBottomAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessagePanelAdapter
import ru.usedesk.chat_gui.chat.messages.adapters.MessagesAdapter
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.UsedeskChatSdk.MAX_FILE_SIZE
import ru.usedesk.chat_sdk.UsedeskChatSdk.MAX_FILE_SIZE_MB
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import java.io.File

internal class MessagesPage : UsedeskFragment() {

    private val viewModel: MessagesViewModel by viewModels()

    private val parentViewModel: ChatViewModel by viewModels(
        ownerProducer = { requireParentFragment() }//TODO: navFragment может помешать
    )

    private lateinit var binding: Binding

    private var messagesAdapter: MessagesAdapter? = null

    private var attachmentDialog: UsedeskAttachmentDialog? = null

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

        init(
            agentName,
            rejectedFileExtensions,
            savedInstanceState
        )

        parentViewModel.modelLiveData.value.toString()

        return binding.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachmentDialog = UsedeskAttachmentDialog.create(
            this,
            viewModel
        )

        registerPermissions()
        registerFiles({ uriList ->
            val files = uriList.map { uri ->
                UsedeskFileInfo.create(
                    requireContext(),
                    uri
                )
            }.toSet()
            attachFiles(files)
        }, { done ->
            val fileName = viewModel.modelLiveData.value.cameraUri
            if (done && fileName != null) {
                val uri = Uri.fromFile(File(requireContext().externalCacheDir, fileName))
                val file = UsedeskFileInfo.create(
                    requireContext(),
                    uri
                )
                attachFiles(setOf(file))
            } else {
                viewModel.resetAction()
            }
        })
    }

    private fun attachFiles(files: Set<UsedeskFileInfo>) {
        val filteredFiles = files.filter { fileInfo ->
            try {
                var size = -1L
                when (fileInfo.uri.scheme) {
                    "content" -> {
                        requireContext().contentResolver.query(
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
                    }
                    "file" -> {
                        requireContext().contentResolver.openAssetFileDescriptor(
                            fileInfo.uri,
                            "r"
                        )?.use {
                            size = it.length
                        }
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
                if (rejectedFiles.size == 1) {
                    R.string.usedesk_file_size_exceeds
                } else {
                    R.string.usedesk_files_size_exceeds
                }, MAX_FILE_SIZE_MB
            )
            val toastText = message + "\n" + rejectedFiles.joinToString(separator = "\n") {
                it.name
            }
            Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show()
        }
        viewModel.attachFiles(filteredFiles)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        unregister()

        messagesAdapter = null

        attachmentDialog = null
    }

    private fun init(
        agentName: String?,
        rejectedFileExtensions: Array<String>,
        savedInstanceState: Bundle?
    ) {
        UsedeskChatSdk.init(requireContext())

        MessagePanelAdapter(
            binding.messagePanel,
            viewModel,
            viewLifecycleOwner
        ) {
            findParent<IUsedeskOnAttachmentClickListener>()?.onAttachmentClick()
                ?: viewModel.showAttachmentPanel(true)
        }

        val mediaPlayerAdapter = findParent<UsedeskChatScreen>()!!.mediaPlayerAdapter

        messagesAdapter = MessagesAdapter(
            binding.rvMessages,
            viewModel,
            viewLifecycleOwner,
            agentName,
            rejectedFileExtensions,
            mediaPlayerAdapter,
            {
                findParent<IUsedeskOnFileClickListener>()?.onFileClick(it)
            },
            {
                findParent<IUsedeskOnUrlClickListener>()?.onUrlClick(it)
                    ?: onUrlClick(it)
            }, {
                findParent<IUsedeskOnDownloadListener>()?.onDownload(it.content, it.name)
            },
            savedInstanceState
        )

        FabToBottomAdapter(
            binding.fabToBottom,
            binding.styleValues,
            viewModel,
            viewLifecycleOwner
        ) {
            messagesAdapter?.scrollToBottom()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        messagesAdapter?.onSave(outState)
    }

    private fun onUrlClick(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
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
}