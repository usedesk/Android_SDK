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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.internal.chat.*
import ru.usedesk.chat_sdk.external.UsedeskChatSdk.startService
import ru.usedesk.chat_sdk.external.UsedeskChatSdk.stopService
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage
import ru.usedesk.common_gui.external.UsedeskViewCustomizer
import ru.usedesk.common_gui.internal.PermissionUtil
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException

class UsedeskChatFragment : Fragment() {
    private var messagePanelAdapter: MessagePanelAdapter? = null
    private var messagesAdapter: MessagesAdapter? = null
    private var offlineFormExpectedAdapter: OfflineFormExpectedAdapter? = null
    private var offlineFormSentAdapter: OfflineFormSentAdapter? = null
    private var attachedFilesAdapter: AttachedFilesAdapter? = null
    private var tvLoading: TextView? = null
    private var ltContent: ViewGroup? = null
    private var filePicker: FilePicker? = null
    private var viewModel: ChatViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filePicker = FilePicker()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = UsedeskViewCustomizer.getInstance()
                .createView(inflater, R.layout.usedesk_fragment_chat, container, false,
                        R.style.Usedesk_Theme_Chat)
        var agentName: String? = null
        val args = arguments
        if (args != null) {
            agentName = args.getString(AGENT_NAME_KEY)
        }
        viewModel = ViewModelProviders.of(this, ChatViewModelFactory(container!!.context))
                .get(ChatViewModel::class.java)
        initUi(view, agentName)
        renderData()
        observeData(viewLifecycleOwner)
        return view
    }

    private fun initUi(view: View, agentName: String?) {
        tvLoading = view.findViewById(R.id.tv_loading)
        ltContent = view.findViewById(R.id.lt_content)
        attachedFilesAdapter = AttachedFilesAdapter(viewModel!!, view.findViewById(R.id.rv_attached_files))
        messagePanelAdapter = MessagePanelAdapter(view, viewModel!!, { v: View? -> openAttachmentDialog() },
                viewLifecycleOwner)
        offlineFormExpectedAdapter = OfflineFormExpectedAdapter(view, viewModel!!, viewLifecycleOwner)
        offlineFormSentAdapter = OfflineFormSentAdapter(view, viewModel!!, viewLifecycleOwner)
        messagesAdapter = MessagesAdapter(view, viewModel!!,
                viewModel!!.messagesLiveData.value,
                viewModel!!.feedbacksLiveData.value, agentName)
    }

    private fun renderData() {
        onMessages(viewModel!!.messagesLiveData.value)
        onFileInfoList(viewModel!!.fileInfoListLiveData.value)
    }

    private fun observeData(lifecycleOwner: LifecycleOwner) {
        viewModel!!.messagesLiveData
                .observe(lifecycleOwner, { usedeskMessages: List<UsedeskMessage>? -> onMessages(usedeskMessages) })
        viewModel!!.fileInfoListLiveData
                .observe(lifecycleOwner, { usedeskFileInfoList: List<UsedeskFileInfo>? -> onFileInfoList(usedeskFileInfoList) })
        viewModel!!.feedbacksLiveData
                .observe(lifecycleOwner, { feedbacks: Set<Int>? -> onFeedbacks(feedbacks) })
        viewModel!!.exceptionLiveData
                .observe(lifecycleOwner, { exception: UsedeskException? -> onException(exception) })
    }

    private fun openAttachmentDialog() {
        val bottomSheetDialog = BottomSheetDialog(context!!)
        val bottomSheetView = UsedeskViewCustomizer.getInstance()
                .createView(activity!!.layoutInflater,
                        R.layout.usedesk_dialog_attachment, null, false, R.style.Usedesk_Theme_Chat)
        bottomSheetView.findViewById<View>(R.id.pick_photo_button)
                .setOnClickListener { view: View? ->
                    bottomSheetDialog.dismiss()
                    PermissionUtil.needReadExternalPermission(getView(),
                            { filePicker!!.pickImage(this) },
                            R.string.need_permission,
                            R.string.settings)
                }
        bottomSheetView.findViewById<View>(R.id.take_photo_button)
                .setOnClickListener { view: View? ->
                    bottomSheetDialog.dismiss()
                    PermissionUtil.needCameraPermission(getView(),
                            { filePicker!!.takePhoto(this) },
                            R.string.need_permission,
                            R.string.settings)
                }
        bottomSheetView.findViewById<View>(R.id.pick_document_button)
                .setOnClickListener { view: View? ->
                    bottomSheetDialog.dismiss()
                    PermissionUtil.needReadExternalPermission(getView(),
                            { filePicker!!.pickDocument(this) },
                            R.string.need_permission,
                            R.string.settings)
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
        if (feedbacks != null) {
            messagesAdapter!!.updateFeedbacks(feedbacks)
        }
    }

    private fun onMessages(usedeskMessages: List<UsedeskMessage>?) {
        val isMessages = usedeskMessages != null
        tvLoading!!.visibility = if (isMessages) View.GONE else View.VISIBLE
        ltContent!!.visibility = if (isMessages) View.VISIBLE else View.GONE
        if (usedeskMessages != null) {
            messagesAdapter!!.updateMessages(usedeskMessages)
        }
    }

    private fun onFileInfoList(usedeskFileInfoList: List<UsedeskFileInfo>?) {
        if (usedeskFileInfoList != null) {
            attachedFilesAdapter!!.update(usedeskFileInfoList)
        }
    }

    override fun onStart() {
        super.onStart()
        stopService(context!!)
    }

    override fun onStop() {
        super.onStop()
        startService(context!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val attachedFileInfoList = filePicker!!.onResult(context!!,
                    requestCode, data)
            if (attachedFileInfoList != null) {
                viewModel!!.setAttachedFileInfoList(attachedFileInfoList)
            }
        }
    }

    companion object {
        private const val AGENT_NAME_KEY = "agentNameKey"

        @JvmOverloads
        fun newInstance(agentName: String? = null): UsedeskChatFragment {
            val args = Bundle()
            if (agentName != null) {
                args.putString(AGENT_NAME_KEY, agentName)
            }
            val fragment = UsedeskChatFragment()
            fragment.arguments = args
            return fragment
        }
    }
}