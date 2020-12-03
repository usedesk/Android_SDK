package ru.usedesk.chat_gui.chat.adapters

import androidx.lifecycle.LifecycleOwner
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.chat_gui.databinding.UsedeskViewOfflineFormBinding
import ru.usedesk.common_gui.hideKeyboard
import ru.usedesk.common_gui.showKeyboard
import ru.usedesk.common_gui.visibleGone

internal class OfflineFormAdapter(
        private val binding: UsedeskViewOfflineFormBinding,
        private val viewModel: ChatViewModel,
        lifecycleOwner: LifecycleOwner,
        private val onBackPressed: () -> Unit
) {

    init {
        binding.tvSend.setOnClickListener {
            hideKeyboard(it)
            viewModel.onSend(
                    binding.etName.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etMessage.text.toString()
            )
        }

        binding.tvBack.setOnClickListener {
            onBackPressed()
        }

        binding.etName.setText(viewModel.configuration.clientName)
        binding.etEmail.setText(viewModel.configuration.email)

        viewModel.offlineFormStateLiveData.observe(lifecycleOwner) {
            onState(it)
        }

        binding.etName.addTextChangedListener(TextChangeListener {
            viewModel.onOfflineFormNameChanged()
        })
        viewModel.nameErrorLiveData.observe(lifecycleOwner) {
            binding.tilName.error = if (it) {
                showKeyboard(binding.etName)
                binding.root.resources.getString(R.string.usedesk_offline_form_name_error)
            } else {
                null
            }
        }

        binding.etEmail.addTextChangedListener(TextChangeListener {
            viewModel.onOfflineFormEmailChanged()
        })
        viewModel.emailErrorLiveData.observe(lifecycleOwner) {
            binding.tilEmail.error = if (it) {
                showKeyboard(binding.etEmail)
                binding.root.resources.getString(R.string.usedesk_offline_form_email_error)
            } else {
                null
            }
        }

        binding.etMessage.addTextChangedListener(TextChangeListener {
            viewModel.onOfflineFormMessageChanged()
        })
        viewModel.messageErrorLiveData.observe(lifecycleOwner) {
            binding.tilMessage.error = if (it) {
                showKeyboard(binding.etMessage)
                binding.root.resources.getString(R.string.usedesk_offline_form_message_error)
            } else {
                null
            }
        }
    }

    fun update(message: String) {
        binding.etMessage.setText(message)
    }

    private fun onState(it: ChatViewModel.OfflineFormState?) {
        when (it) {
            ChatViewModel.OfflineFormState.DEFAULT -> {
                showViews(
                        offlineText = true,
                        fields = true,
                        send = true
                )
            }
            ChatViewModel.OfflineFormState.SENDING -> {
                showViews(
                        offlineText = true,
                        fields = true,
                        loading = true,
                )
            }
            ChatViewModel.OfflineFormState.FAILED_TO_SEND -> {
                showViews(
                        offlineText = true,
                        fields = true,
                        error = true,
                        send = true
                )
            }
            ChatViewModel.OfflineFormState.SENT_SUCCESSFULLY -> {
                showViews(
                        sentText = true,
                        back = true
                )
            }
        }
    }

    private fun showViews(
            offlineText: Boolean = false,
            sentText: Boolean = false,
            fields: Boolean = false,
            error: Boolean = false,
            loading: Boolean = false,
            send: Boolean = false,
            back: Boolean = false
    ) {
        binding.tvOfflineText.visibility = visibleGone(offlineText)
        binding.tvSentText.visibility = visibleGone(sentText)
        binding.tilName.visibility = visibleGone(fields)
        binding.tilEmail.visibility = visibleGone(fields)
        binding.tilMessage.visibility = visibleGone(fields)
        binding.tvError.visibility = visibleGone(error)
        binding.pbLoading.visibility = visibleGone(loading)
        binding.tvSend.visibility = visibleGone(send)
        binding.tvBack.visibility = visibleGone(back)
    }
}