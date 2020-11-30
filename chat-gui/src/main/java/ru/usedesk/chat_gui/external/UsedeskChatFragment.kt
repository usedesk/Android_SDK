package ru.usedesk.chat_gui.external

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.UsedeskDialogAttachmentBinding
import ru.usedesk.chat_gui.databinding.UsedeskScreenChatBinding
import ru.usedesk.chat_gui.internal.chat.*
import ru.usedesk.chat_sdk.external.UsedeskChatSdk
import ru.usedesk.common_gui.external.UsedeskToolbar
import ru.usedesk.common_gui.internal.*
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException

class UsedeskChatFragment : UsedeskFragment(R.style.Usedesk_Theme_Chat) {

    private val viewModel: ChatViewModel by viewModels()
    private val filePicker: UsedeskFilePicker = UsedeskFilePicker()

    private lateinit var binding: UsedeskScreenChatBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        doInit {
            val agentName: String? = argsGetString(AGENT_NAME_KEY)

            binding = inflateBinding(inflater,
                    container,
                    R.layout.usedesk_screen_chat,
                    defaultStyleId)

            val title = getStringFromStyle(R.attr.usedesk_screen_chat_title)

            init(agentName)
            UsedeskToolbar(requireActivity() as AppCompatActivity, binding.toolbar).apply {
                setTitle(title)
                setBackButton {
                    requireActivity().onBackPressed()
                }
            }
        }

        return binding.root
    }

    private fun init(agentName: String?) {
        UsedeskChatSdk.init(requireContext(), viewModel.actionListenerRx)

        viewModel.init()

        MessagePanelAdapter(binding.messagePanel, viewModel, viewLifecycleOwner) {
            openAttachmentDialog()
        }

        OfflineFormExpectedAdapter(binding.root, viewModel, viewLifecycleOwner)
        OfflineFormSentAdapter(binding.root, viewModel, viewLifecycleOwner)

        ItemsAdapter(viewModel,
                binding.rvMessages,
                agentName,
                viewLifecycleOwner) { file ->
            requireActivity().also {
                if (it is IUsedeskOnFileClickListener) {
                    it.onFileClick(file)
                }
            }
        }
        viewModel.feedbacksLiveData.observe(viewLifecycleOwner) {
            onFeedbacks(it)
        }
        viewModel.exceptionLiveData.observe(viewLifecycleOwner) {
            onException(it)
        }
        viewModel.chatItemsLiveData.observe(viewLifecycleOwner) {
            showInstead(binding.ltContent, binding.tvLoading, it != null)
        }
    }

    private fun openAttachmentDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())//TODO: сделать отдельным файлом, + сделать кастомизируемым
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