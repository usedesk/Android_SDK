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
        private val viewModel: ChatViewModel,
        lifecycleOwner: LifecycleOwner,
        private val onSuccessfully: () -> Unit,
        private val onFailed: () -> Unit
) {

    init {
        val resources = binding.rootView.resources

        binding.tvSend.setOnClickListener {
            hideKeyboard(it)
            viewModel.onSend(
                    binding.etName.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etMessage.text.toString()
            )
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
                resources.getString(R.string.usedesk_offline_form_name_error)
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
                resources.getString(R.string.usedesk_offline_form_email_error)
            } else {
                null
            }
        }

        binding.etMessage.addTextChangedListener(TextChangeListener {
            viewModel.onOfflineFormMessageChanged()
            updateActionButton(it)
        })

        viewModel.messageErrorLiveData.observe(lifecycleOwner) {
            binding.tilMessage.error = if (it) {
                showKeyboard(binding.etMessage)
                resources.getString(R.string.usedesk_offline_form_message_error)
            } else {
                null
            }
        }

        updateActionButton("")
    }

    private fun updateActionButton(message: String) {
        val attr = if (message.isEmpty()) {
            binding.tvSend.isEnabled = false
            R.attr.usedesk_chat_offline_form_action_button_background_disabled
        } else {
            binding.tvSend.isEnabled = true
            R.attr.usedesk_chat_offline_form_action_button_background_enabled
        }

        val colorId = UsedeskResourceManager.getStyleValues(
                binding.rootView.context,
                R.style.Usedesk_Chat
        ).getColor(attr)
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

    internal class Binding(rootView: View) : UsedeskBinding(rootView) {
        val tvOfflineText: TextView = rootView.findViewById(R.id.tv_offline_form_text)
        val tilEmail: TextInputLayout = rootView.findViewById(R.id.til_offline_form_email)
        val etEmail: EditText = rootView.findViewById(R.id.et_offline_form_email)
        val tilName: TextInputLayout = rootView.findViewById(R.id.til_offline_form_name)
        val etName: EditText = rootView.findViewById(R.id.et_offline_form_name)
        val tilMessage: TextInputLayout = rootView.findViewById(R.id.til_offline_form_message)
        val etMessage: EditText = rootView.findViewById(R.id.et_offline_form_message)
        val tvSend: TextView = rootView.findViewById(R.id.tv_offline_form_send)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_offline_form_loading)
        val lAction: ViewGroup = rootView.findViewById(R.id.l_offline_form_action)
    }
}