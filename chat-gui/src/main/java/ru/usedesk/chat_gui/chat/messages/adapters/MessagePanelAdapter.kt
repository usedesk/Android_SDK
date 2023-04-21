
package ru.usedesk.chat_gui.chat.messages.adapters

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskTextChangeListener
import ru.usedesk.common_gui.onEachWithOld

internal class MessagePanelAdapter(
    private val binding: Binding,
    private val viewModel: MessagesViewModel,
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    onClickAttach: View.OnClickListener
) {
    private val attachedFilesAdapter: AttachedFilesAdapter

    init {
        binding.ivAttachFile.setOnClickListener(onClickAttach)
        binding.ivSend.setOnClickListener { onSendClick() }
        attachedFilesAdapter = AttachedFilesAdapter(
            binding.rvAttachedFiles,
            viewModel,
            lifecycleCoroutineScope
        )

        binding.etMessage.run {
            setText(viewModel.modelFlow.value.messageDraft.text)
            addTextChangedListener(UsedeskTextChangeListener {
                viewModel.onEvent(MessagesViewModel.Event.MessageChanged(it))
            })
        }
        viewModel.modelFlow.onEachWithOld(lifecycleCoroutineScope) { old, new ->
            if (old?.messageDraft?.isNotEmpty != new.messageDraft.isNotEmpty) {
                binding.ivSend.isEnabled = new.messageDraft.isNotEmpty
                binding.ivSend.alpha = binding.sendAlpha * when {
                    new.messageDraft.isNotEmpty -> 1f
                    else -> 0.5f
                }
            }
        }
    }

    private fun onSendClick() {
        viewModel.onEvent(MessagesViewModel.Event.SendDraft)
        binding.etMessage.setText("")
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val ivAttachFile: ImageView = rootView.findViewById(R.id.iv_attach_file)
        val ivSend: ImageView = rootView.findViewById(R.id.iv_send)
        val etMessage: EditText = rootView.findViewById(R.id.et_message)
        val rvAttachedFiles: RecyclerView = rootView.findViewById(R.id.rv_attached_files)
        val sendAlpha = styleValues.getStyleValues(R.attr.usedesk_chat_screen_send_image)
            .findString(android.R.attr.alpha)?.toFloatOrNull() ?: 1.0f
    }
}