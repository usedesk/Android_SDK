package ru.usedesk.chat_gui.chat.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.textfield.TextInputLayout
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.common_gui.*

internal class OfflineFormAdapter(
        private val binding: Binding,
        private val rootStyleValues: UsedeskResourceManager.StyleValues,
        private val viewModel: ChatViewModel,
        private val onSuccessfully: () -> Unit,
        private val onFailed: () -> Unit
) : IUsedeskAdapter<ChatViewModel> {

    init {
        binding.tvSend.setOnClickListener {
            hideKeyboard(it)
            viewModel.onSend(
                    binding.etName.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etMessage.text.toString()
            )
        }

        binding.etName.setText(viewModel.configuration.clientName)
        binding.etEmail.setText(viewModel.configuration.clientEmail)


        binding.etName.addTextChangedListener(TextChangeListener {
            viewModel.onOfflineFormNameChanged()
        })

        binding.etEmail.addTextChangedListener(TextChangeListener {
            viewModel.onOfflineFormEmailChanged()
        })

        binding.etMessage.addTextChangedListener(TextChangeListener {
            viewModel.onOfflineFormMessageChanged()
            updateActionButton(it)
        })

        updateActionButton("")
    }

    override fun onLiveData(viewModel: ChatViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.offlineFormStateLiveData.observe(lifecycleOwner) {
            onState(it)
        }

        viewModel.nameErrorLiveData.observe(lifecycleOwner) {
            binding.tilName.error = if (it) {
                showKeyboard(binding.etName)
                binding.styleValues
                        .getStyleValues(R.attr.usedesk_chat_screen_offline_form_name_input_layout)
                        .getString(R.attr.usedesk_text_1)
            } else {
                null
            }
        }

        viewModel.emailErrorLiveData.observe(lifecycleOwner) {
            binding.tilEmail.error = if (it) {
                showKeyboard(binding.etEmail)
                binding.styleValues
                        .getStyleValues(R.attr.usedesk_chat_screen_offline_form_email_input_layout)
                        .getString(R.attr.usedesk_text_1)
            } else {
                null
            }
        }

        viewModel.messageErrorLiveData.observe(lifecycleOwner) {
            binding.tilMessage.error = if (it) {
                showKeyboard(binding.etMessage)
                binding.styleValues
                        .getStyleValues(R.attr.usedesk_chat_screen_offline_form_message_input_layout)
                        .getString(R.attr.usedesk_text_1)
            } else {
                null
            }
        }
    }

    private fun updateActionButton(message: String) {
        val attr = if (message.isEmpty()) {
            binding.tvSend.isEnabled = false
            R.attr.usedesk_chat_screen_offline_form_action_disabled_background
        } else {
            binding.tvSend.isEnabled = true
            R.attr.usedesk_chat_screen_offline_form_action_enabled_background
        }

        val colorId = rootStyleValues.getColor(attr)
        binding.lAction.setBackgroundColor(colorId)
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
                onFailed()
                showViews(
                        offlineText = true,
                        fields = true,
                        send = true
                )
            }
            ChatViewModel.OfflineFormState.SENT_SUCCESSFULLY -> {
                onSuccessfully()
                showViews(
                        offlineText = true,
                        fields = true,
                        send = true
                )
            }
        }
    }

    private fun showViews(
            offlineText: Boolean = false,
            fields: Boolean = false,
            loading: Boolean = false,
            send: Boolean = false
    ) {
        binding.tvOfflineText.visibility = visibleGone(offlineText)
        binding.tilName.visibility = visibleGone(fields)
        binding.tilEmail.visibility = visibleGone(fields)
        binding.tilMessage.visibility = visibleGone(fields)
        binding.pbLoading.visibility = visibleGone(loading)
        binding.tvSend.visibility = visibleGone(send)
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvOfflineText: TextView = rootView.findViewById(R.id.tv_offline_form_text)
        val tilEmail: TextInputLayout = rootView.findViewById(R.id.til_offline_form_email)
        val etEmail: EditText = rootView.findViewById(R.id.et_offline_form_email)
        val tilName: TextInputLayout = rootView.findViewById(R.id.til_offline_form_name)
        val etName: EditText = rootView.findViewById(R.id.et_offline_form_name)
        val tilMessage: TextInputLayout = rootView.findViewById(R.id.til_offline_form_message)
        val etMessage: EditText = rootView.findViewById(R.id.et_offline_form_message)
        val tvSend: TextView = rootView.findViewById(R.id.tv_offline_form_send)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_offline_form_loading)
        val lAction: ViewGroup = rootView.findViewById(R.id.l_offline_form_send)
    }
}