package ru.usedesk.chat_gui.chat.messages.adapters

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskTextChangeListener

internal class MessagePanelAdapter(
    private val binding: Binding,
    private val viewModel: MessagesViewModel,
    lifecycleOwner: LifecycleOwner,
    onClickAttach: View.OnClickListener
) {

    private val attachedFilesAdapter: AttachedFilesAdapter

    init {
        binding.ivAttachFile.setOnClickListener(onClickAttach)
        binding.ivSend.setOnClickListener {
            onSendClick()
        }
        binding.etMessage.setText(viewModel.message)
        binding.etMessage.addTextChangedListener(UsedeskTextChangeListener {
            viewModel.onMessageChanged(it)
        })
        attachedFilesAdapter =
            AttachedFilesAdapter(
                binding.rvAttachedFiles,
                viewModel,
                lifecycleOwner
            )
    }

    private fun onSendClick() {
        viewModel.onSend(binding.etMessage.text.toString().trim { it <= ' ' })
        binding.etMessage.setText("")
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val ivAttachFile: ImageView = rootView.findViewById(R.id.iv_attach_file)
        val ivSend: ImageView = rootView.findViewById(R.id.iv_send)
        val etMessage: EditText = rootView.findViewById(R.id.et_message)
        val rvAttachedFiles: RecyclerView = rootView.findViewById(R.id.rv_attached_files)
    }

    companion object {
        private const val MESSAGE_KEY = "messageKey"
    }
}