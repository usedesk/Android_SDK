package ru.usedesk.chat_gui.chat.messages.adapters

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskTextChangeListener
import ru.usedesk.common_gui.insetsAsPaddings
import ru.usedesk.common_gui.onEachWithOld
import ru.usedesk.chat_gui.R as chatR

internal class MessagePanelAdapter(
    private val binding: Binding,
    private val viewModel: MessagesViewModel,
    supportWindowInsets: Boolean,
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    onClickAttach: View.OnClickListener,
) {
    private val attachedFilesAdapter: AttachedFilesAdapter

    init {
        if (supportWindowInsets) {
            binding.rootView.insetsAsPaddings(ignoreStatusBar = true)
        }
        binding.ivAttachFile.setOnClickListener(onClickAttach)
        binding.ivSend.setOnClickListener { onSendClick() }
        attachedFilesAdapter = AttachedFilesAdapter(
            binding.rvAttachedFiles,
            viewModel,
            lifecycleCoroutineScope
        )

        binding.etMessage.run {
            isSaveEnabled = false
            setText(viewModel.modelFlow.value.messageDraft.text)
            setSelection(text?.length ?: 0)
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
        val ivAttachFile: ImageView = rootView.findViewById(chatR.id.iv_attach_file)
        val ivSend: ImageView = rootView.findViewById(chatR.id.iv_send)
        val etMessage: EditText = rootView.findViewById(chatR.id.et_message)
        val rvAttachedFiles: RecyclerView = rootView.findViewById(chatR.id.rv_attached_files)
        val sendAlpha = styleValues.getStyleValues(chatR.attr.usedesk_chat_screen_send_image)
            .findString(android.R.attr.alpha)?.toFloatOrNull() ?: 1.0f
    }
}