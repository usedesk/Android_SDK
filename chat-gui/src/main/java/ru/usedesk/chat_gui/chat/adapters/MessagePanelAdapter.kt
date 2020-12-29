package ru.usedesk.chat_gui.chat.adapters

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.TextChangeListener
import ru.usedesk.common_gui.UsedeskBinding

internal class MessagePanelAdapter(
        private val binding: Binding,
        private val viewModel: ChatViewModel,
        lifecycleOwner: LifecycleOwner,
        onClickAttach: View.OnClickListener
) {

    private val attachedFilesAdapter: AttachedFilesAdapter

    init {
        binding.ivAttachFile.setOnClickListener(onClickAttach)
        binding.ivSend.setOnClickListener {
            onSendClick()
        }
        binding.etMessage.setText(viewModel.messageLiveData.value)
        binding.etMessage.addTextChangedListener(TextChangeListener {
            viewModel.onMessageChanged(it)
        })
        attachedFilesAdapter = AttachedFilesAdapter(viewModel, binding.rvAttachedFiles)

        onFileInfoList(viewModel.fileInfoListLiveData.value)

        viewModel.fileInfoListLiveData.observe(lifecycleOwner) {
            onFileInfoList(it)
        }
    }

    private fun onSendClick() {
        viewModel.onSend(binding.etMessage.text.toString().trim { it <= ' ' })
        binding.etMessage.setText("")
    }

    private fun onFileInfoList(usedeskFileInfoList: List<UsedeskFileInfo>?) {
        if (usedeskFileInfoList != null) {
            attachedFilesAdapter.update(usedeskFileInfoList)
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val ivAttachFile: ImageView = rootView.findViewById(R.id.iv_attach_file)
        val ivSend: ImageView = rootView.findViewById(R.id.iv_send)
        val etMessage: EditText = rootView.findViewById(R.id.et_message)
        val rvAttachedFiles: RecyclerView = rootView.findViewById(R.id.rv_attached_files)
    }
}