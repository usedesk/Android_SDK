package ru.usedesk.chat_gui.external

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.UsedeskDialogAttachmentBinding
import ru.usedesk.chat_gui.databinding.UsedeskFragmentChatBinding
import ru.usedesk.chat_gui.internal.chat.*
import ru.usedesk.chat_sdk.external.UsedeskChatSdk
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.common_gui.internal.PermissionUtil
import ru.usedesk.common_gui.internal.argsGetString
import ru.usedesk.common_gui.internal.inflateBinding
import ru.usedesk.common_gui.internal.showInstead
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException

class UsedeskChatFragment : Fragment() {

    private val viewModel: ChatViewModel by viewModels()

    private val filePicker: FilePicker = FilePicker()

    private lateinit var attachedFilesAdapter: AttachedFilesAdapter

    private lateinit var binding: UsedeskFragmentChatBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val agentName: String? = argsGetString(arguments, AGENT_NAME_KEY)

        binding = inflateBinding(inflater,
                container,
                R.layout.usedesk_fragment_chat,
                R.style.Usedesk_Theme_Chat)

        if (savedInstanceState == null) {
            init(agentName)
        }

        return binding.root
    }

    private fun init(agentName: String?) {
        UsedeskChatSdk.init(requireContext(), viewModel.actionListenerRx)

        viewModel.init()

        attachedFilesAdapter = AttachedFilesAdapter(viewModel, binding.rvMessages)
        MessagePanelAdapter(binding.root, viewModel, viewLifecycleOwner) {
            openAttachmentDialog()
        }

        OfflineFormExpectedAdapter(binding.root, viewModel, viewLifecycleOwner)
        OfflineFormSentAdapter(binding.root, viewModel, viewLifecycleOwner)

        ItemsAdapter(viewModel,
                binding.rvMessages,
                agentName,
                viewLifecycleOwner)

        onFileInfoList(viewModel.fileInfoListLiveData.value)

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
        val bottomSheetBinding = inflateBinding<UsedeskDialogAttachmentBinding>(layoutInflater,
                binding.lRoot,
                R.layout.usedesk_dialog_attachment,
                R.style.Usedesk_Theme_Chat).apply {

            pickPhotoButton.setOnClickListener {
                bottomSheetDialog.dismiss()
                PermissionUtil.needReadExternalPermission(binding.root,
                        R.string.need_permission,
                        R.string.settings
                ) {
                    filePicker.pickImage(this@UsedeskChatFragment)
                }
            }

            takePhotoButton.setOnClickListener {
                bottomSheetDialog.dismiss()
                PermissionUtil.needCameraPermission(binding.root,
                        R.string.need_permission,
                        R.string.settings
                ) {
                    filePicker.takePhoto(this@UsedeskChatFragment)
                }
            }

            pickDocumentButton.setOnClickListener {
                bottomSheetDialog.dismiss()
                PermissionUtil.needReadExternalPermission(binding.root,
                        R.string.need_permission,
                        R.string.settings
                ) {
                    filePicker.pickDocument(this@UsedeskChatFragment)
                }
            }

            bottomSheetDialog.setContentView(root)
            bottomSheetDialog.show()
        }
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

    private fun onFileInfoList(usedeskFileInfoList: List<UsedeskFileInfo>?) {
        showInstead(binding.ltContent, binding.tvLoading, usedeskFileInfoList != null)
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
        private const val AGENT_NAME_KEY = "agentNameKey"

        @JvmOverloads
        @JvmStatic
        fun newInstance(agentName: String? = null): UsedeskChatFragment {
            return UsedeskChatFragment().apply {
                arguments = Bundle().apply {
                    if (agentName != null) {
                        putString(AGENT_NAME_KEY, agentName)
                    }
                }
            }
        }
    }
}