package ru.usedesk.chat_gui.external

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.internal.chat.*
import ru.usedesk.chat_sdk.external.UsedeskChatSdk
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage
import ru.usedesk.common_gui.internal.*
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException

class UsedeskChatFragment : Fragment() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var rootView: ViewGroup
    private lateinit var tvLoading: TextView
    private lateinit var ltContent: ViewGroup

    private lateinit var filePicker: FilePicker

    private lateinit var attachedFilesAdapter: AttachedFilesAdapter

    private var themeId = R.style.Usedesk_Theme_Chat

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        themeId = argsGetInt(arguments, THEME_ID_KEY, themeId)
        val agentName: String? = argsGetString(arguments, AGENT_NAME_KEY)

        rootView = inflateFragment(inflater, container, themeId, R.layout.usedesk_fragment_chat)

        filePicker = FilePicker()

        if (savedInstanceState == null) {
            UsedeskChatSdk.init(requireContext().applicationContext, viewModel.actionListenerRx)
        }

        init(agentName)
        return rootView
    }

    private fun init(agentName: String?) {
        viewModel.init()

        tvLoading = rootView.findViewById(R.id.tv_loading)
        ltContent = rootView.findViewById(R.id.lt_content)

        attachedFilesAdapter = AttachedFilesAdapter(viewModel, rootView.findViewById(R.id.rv_attached_files))
        MessagePanelAdapter(rootView, viewModel, { openAttachmentDialog() },
                viewLifecycleOwner)
        OfflineFormExpectedAdapter(rootView, viewModel, viewLifecycleOwner)
        OfflineFormSentAdapter(rootView, viewModel, viewLifecycleOwner)

        ItemsAdapter(viewModel,
                rootView.findViewById(R.id.rv_messages),
                viewLifecycleOwner)

        renderData()
        observeData()
    }

    private fun renderData() {
        onMessages(viewModel.messagesLiveData.value)
        onFileInfoList(viewModel.fileInfoListLiveData.value)
    }

    private fun observeData() {
        viewModel.messagesLiveData.observe(viewLifecycleOwner, {
            onMessages(it)
        })
        viewModel.fileInfoListLiveData.observe(viewLifecycleOwner, {
            onFileInfoList(it)
        })
        viewModel.feedbacksLiveData.observe(viewLifecycleOwner, {
            onFeedbacks(it)
        })
        viewModel.exceptionLiveData.observe(viewLifecycleOwner, {
            onException(it)
        })
    }

    private fun openAttachmentDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())//TODO: сделать отдельным файлом
        val bottomSheetView = layoutInflater.inflate(R.layout.usedesk_dialog_attachment, rootView, false)

        bottomSheetView.findViewById<View>(R.id.pick_photo_button)
                .setOnClickListener {
                    bottomSheetDialog.dismiss()
                    PermissionUtil.needReadExternalPermission(rootView,
                            R.string.need_permission,
                            R.string.settings
                    ) {
                        filePicker.pickImage(this)
                    }
                }

        bottomSheetView.findViewById<View>(R.id.take_photo_button)
                .setOnClickListener {
                    bottomSheetDialog.dismiss()
                    PermissionUtil.needCameraPermission(rootView,
                            R.string.need_permission,
                            R.string.settings
                    ) {
                        filePicker.takePhoto(this)
                    }
                }

        bottomSheetView.findViewById<View>(R.id.pick_document_button)
                .setOnClickListener {
                    bottomSheetDialog.dismiss()
                    PermissionUtil.needReadExternalPermission(rootView,
                            R.string.need_permission,
                            R.string.settings
                    ) {
                        filePicker.pickDocument(this)
                    }
                }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun onException(exception: UsedeskException?) {
        if (exception != null) {
            var message = exception.message
            if (message == null) {
                message = exception.toString()
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onFeedbacks(feedbacks: Set<Int>?) {
        /*if (feedbacks != null) {
            messagesAdapter.updateFeedbacks(feedbacks);
        }*/
    }

    private fun onMessages(usedeskMessages: List<UsedeskMessage>?) {
        val isMessages = usedeskMessages != null
        showInstead(ltContent, tvLoading, isMessages)

        /*if (usedeskMessages != null) {
            messagesAdapter.updateMessages(usedeskMessages);
        }*/
    }

    private fun onFileInfoList(usedeskFileInfoList: List<UsedeskFileInfo>?) {
        if (usedeskFileInfoList != null) {
            attachedFilesAdapter.update(usedeskFileInfoList)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val attachedFiles = filePicker.onResult(requireContext(),
                    requestCode, data)
            if (attachedFiles != null) {
                viewModel.setAttachedFiles(attachedFiles)
            }
        }
    }

    companion object {
        internal const val THEME_ID_KEY = "themeIdKey"
        private const val AGENT_NAME_KEY = "agentNameKey"

        @JvmOverloads
        @JvmStatic
        fun newInstance(themeId: Int? = null,
                        agentName: String? = null): UsedeskChatFragment {
            return UsedeskChatFragment().apply {
                arguments = Bundle().apply {
                    if (themeId != null) {
                        putInt(THEME_ID_KEY, themeId)
                    }
                    if (agentName != null) {
                        putString(AGENT_NAME_KEY, agentName)
                    }
                }
            }
        }
    }
}