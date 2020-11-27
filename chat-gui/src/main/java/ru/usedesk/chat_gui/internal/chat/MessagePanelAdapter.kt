package ru.usedesk.chat_gui.internal.chat

import android.view.View
import androidx.lifecycle.LifecycleOwner
import ru.usedesk.chat_gui.databinding.UsedeskViewMessagePanelBinding
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.common_gui.internal.visibleGone

class MessagePanelAdapter(
        private val binding: UsedeskViewMessagePanelBinding,
        private val viewModel: ChatViewModel,
        lifecycleOwner: LifecycleOwner,
        onClickAttach: View.OnClickListener
) {

    private val attachedFilesAdapter: AttachedFilesAdapter

    init {
        binding.attachFileImageView.setOnClickListener(onClickAttach)
        binding.sendImageView.setOnClickListener {
            onSendClick()
        }
        onMessagePanelState(viewModel.messagePanelStateLiveData.value)
        viewModel.messagePanelStateLiveData.observe(lifecycleOwner) {
            onMessagePanelState(it)
        }
        binding.messageEditText.setText(viewModel.messageLiveData.value)
        binding.messageEditText.addTextChangedListener(TextChangeListener {
            viewModel.onMessageChanged(it)
        })
        attachedFilesAdapter = AttachedFilesAdapter(viewModel, binding.rvAttachedFiles)

        onFileInfoList(viewModel.fileInfoListLiveData.value)

        viewModel.fileInfoListLiveData.observe(lifecycleOwner) {
            onFileInfoList(it)
        }
    }

    private fun onMessagePanelState(messagePanelState: MessagePanelState?) {
        val messagePanel = (messagePanelState != null
                && messagePanelState == MessagePanelState.MESSAGE_PANEL)
        binding.root.visibility = visibleGone(messagePanel)
        if (messagePanel) {
            binding.messageEditText.setText(viewModel.messageLiveData.value)
        }
    }

    private fun onSendClick() {
        viewModel.onSend(binding.messageEditText.text.toString().trim { it <= ' ' })
        binding.messageEditText.setText("")
    }

    private fun onFileInfoList(usedeskFileInfoList: List<UsedeskFileInfo>?) {
        if (usedeskFileInfoList != null) {
            attachedFilesAdapter.update(usedeskFileInfoList)
        }
    }
}