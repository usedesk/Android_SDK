package ru.usedesk.chat_gui.chat.messages.adapters

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.common_gui.IUsedeskAdapter
import ru.usedesk.common_gui.TextChangeListener
import ru.usedesk.common_gui.UsedeskBinding

internal class MessagePanelAdapter(
        private val binding: Binding,
        private val viewModel: MessagesViewModel,
        onClickAttach: View.OnClickListener
) : IUsedeskAdapter<MessagesViewModel> {

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
    }

    override fun onLiveData(viewModel: MessagesViewModel, lifecycleOwner: LifecycleOwner) {
        attachedFilesAdapter.onLiveData(viewModel, lifecycleOwner)
    }

    private fun onSendClick() {
        viewModel.onSend(binding.etMessage.text.toString().trim { it <= ' ' })
        binding.etMessage.setText("")
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val ivAttachFile: ImageView = rootView.findViewById(R.id.iv_attach_file)
        val ivSend: ImageView = rootView.findViewById(R.id.iv_send)
        val etMessage: EditText = rootView.findViewById(R.id.et_message)
        val rvAttachedFiles: RecyclerView = rootView.findViewById(R.id.rv_attached_files)
    }
}