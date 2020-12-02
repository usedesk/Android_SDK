package ru.usedesk.chat_gui.external

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.UsedeskScreenChatBinding
import ru.usedesk.chat_gui.external.attachpanel.UsedeskDialogAttachmentPanel
import ru.usedesk.chat_gui.internal.chat.*
import ru.usedesk.chat_sdk.external.UsedeskChatSdk
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.common_gui.external.UsedeskToolbar
import ru.usedesk.common_gui.internal.UsedeskFragment
import ru.usedesk.common_gui.internal.inflateBinding
import ru.usedesk.common_gui.internal.showInstead

class UsedeskChatFragment : UsedeskFragment(R.style.Usedesk_Theme_Chat) {

    private val viewModel: ChatViewModel by viewModels()

    private lateinit var binding: UsedeskScreenChatBinding
    private lateinit var attachmentDialog: UsedeskDialogAttachmentPanel

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachmentDialog = UsedeskDialogAttachmentPanel(this)
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
                viewLifecycleOwner, { file ->
            getParentListener<IUsedeskOnFileClickListener>()?.onFileClick(file)
        }, { html ->
            getParentListener<IUsedeskOnHtmlClickListener>()?.onHtmlClick(html)
        }, { url ->
            getParentListener<IUsedeskOnUrlClickListener>()?.onUrlClick(url)
                    ?: onUrlClick(url)
        })
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

    private fun onUrlClick(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private inline fun <reified T> getParentListener(): T? {
        return when {
            activity is T -> {
                activity as T
            }
            parentFragment is T -> {
                parentFragment as T
            }
            else -> {
                null
            }
        }
    }

    private fun openAttachmentDialog() {
        getParentListener<IUsedeskOnAttachmentClickListener>()?.onAttachmentClick()
                ?: attachmentDialog.show()
    }

    private fun onException(exception: Exception) {
        exception.printStackTrace()
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
        attachmentDialog.onActivityResult(requestCode, resultCode, data)
    }

    fun setAttachedFiles(attachedFiles: List<UsedeskFileInfo>) {
        viewModel.setAttachedFiles(attachedFiles)
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