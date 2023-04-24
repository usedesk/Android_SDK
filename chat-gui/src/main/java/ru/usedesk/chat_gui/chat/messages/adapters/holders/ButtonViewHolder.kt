
package ru.usedesk.chat_gui.chat.messages.adapters.holders

import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.Event
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter.ButtonBinding
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter.Item
import ru.usedesk.chat_gui.chat.messages.adapters.MessageFormAdapter.Item.ItemButton
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.common_gui.visibleGone
import ru.usedesk.common_gui.visibleInvisible

internal class ButtonViewHolder(
    private val binding: ButtonBinding,
    private val onEvent: (Event) -> Unit
) : BaseViewHolder(binding.rootView) {

    private val applyTitleDefault = binding.styleValues.getString(R.attr.usedesk_text_1)
    private val applyTitleSuccsessfull = binding.styleValues.getString(R.attr.usedesk_text_2)
    private val applyTitleFailed = binding.styleValues.getString(R.attr.usedesk_text_3)
    private val backgroundDefault = binding.styleValues.getColor(R.attr.usedesk_background_color_1)
    private val backgroundSuccsessfull =
        binding.styleValues.getColor(R.attr.usedesk_background_color_2)
    private val backgroundFailed = binding.styleValues.getColor(R.attr.usedesk_background_color_3)

    init {
        binding.pbLoading.visibility = View.INVISIBLE
    }

    override fun bind(
        messageId: Long,
        item: Item,
        scope: CoroutineScope,
        stateFlow: StateFlow<MessagesViewModel.State>
    ) {
        item as ItemButton
        when (item.button) {
            null -> binding.apply {
                var formState: UsedeskForm.State? = null
                stateFlow.onEach { state ->
                    val form = state.formMap[messageId]
                    if (form != null) {
                        val newFormState = form.state
                        if (newFormState != formState) {
                            formState = newFormState
                            update(
                                messageId,
                                newFormState
                            )
                        }
                    }
                }.launchIn(viewHolderScope)
            }
            else -> {
                binding.lBtn.setCardBackgroundColor(backgroundDefault)
                binding.pbLoading.visibility = View.GONE
                binding.tvTitle.run {
                    text = item.button.name
                    setOnClickListener { onEvent(Event.MessageButtonClick(item.button)) }
                }
            }
        }
    }

    private fun update(
        messageId: Long,
        state: UsedeskForm.State
    ) {
        binding.tvTitle.apply {
            val enable = when (state) {
                UsedeskForm.State.LOADED,
                UsedeskForm.State.LOADING_FAILED,
                UsedeskForm.State.SENDING_FAILED -> true
                else -> false
            }
            binding.tvTitle.text = when (state) {
                UsedeskForm.State.LOADING_FAILED,
                UsedeskForm.State.SENDING_FAILED -> applyTitleFailed
                UsedeskForm.State.SENT -> applyTitleSuccsessfull
                else -> applyTitleDefault
            }
            visibility = visibleInvisible(
                when (state) {
                    UsedeskForm.State.LOADING_FAILED,
                    UsedeskForm.State.LOADED,
                    UsedeskForm.State.SENDING_FAILED,
                    UsedeskForm.State.SENT -> true
                    else -> false
                }
            )
            isClickable = enable
            isFocusable = enable
            if (enable) {
                setOnClickListener { onEvent(Event.FormApplyClick(messageId)) }
            }
        }
        binding.lBtn.apply {
            visibility = visibleInvisible(
                when (state) {
                    UsedeskForm.State.NOT_LOADED,
                    UsedeskForm.State.LOADING -> false
                    else -> true
                }
            )
            setCardBackgroundColor(
                when (state) {
                    UsedeskForm.State.LOADING_FAILED,
                    UsedeskForm.State.SENDING_FAILED -> backgroundFailed
                    UsedeskForm.State.SENT -> backgroundSuccsessfull
                    else -> backgroundDefault
                }
            )
        }
        binding.pbLoading.apply {
            visibility = visibleGone(
                when (state) {
                    UsedeskForm.State.LOADING,
                    UsedeskForm.State.SENDING -> true
                    else -> false
                }
            )
        }
    }
}