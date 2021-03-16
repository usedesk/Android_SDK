package ru.usedesk.chat_gui.chat.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
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

    private val nameAdapter = UsedeskCommonFieldTextAdapter(binding.name)
    private val emailAdapter = UsedeskCommonFieldTextAdapter(binding.email)
    private val messageAdapter = UsedeskCommonFieldTextAdapter(binding.message)
    private val fieldsAdapter = AdditionalAdapter(binding.rvAdditional)

    init {
        binding.tvSend.setOnClickListener {
            hideKeyboard(it)
            viewModel.onSend(
                    nameAdapter.getText(),
                    emailAdapter.getText(),
                    messageAdapter.getText()
            )
        }

        nameAdapter.setText(viewModel.configuration.clientName)
        emailAdapter.setText(viewModel.configuration.clientEmail)


        nameAdapter.setTextChangeListener {
            viewModel.onOfflineFormNameChanged()
        }

        emailAdapter.setTextChangeListener {
            viewModel.onOfflineFormEmailChanged()
        }

        messageAdapter.setTextChangeListener {
            viewModel.onOfflineFormMessageChanged()
            updateActionButton(it)
        }

        updateActionButton("")
    }

    override fun onLiveData(viewModel: ChatViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.offlineFormStateLiveData.observe(lifecycleOwner) {
            onState(it)
        }

        viewModel.nameErrorLiveData.observe(lifecycleOwner) {
            nameAdapter.showError(it)
        }

        viewModel.emailErrorLiveData.observe(lifecycleOwner) {
            emailAdapter.showError(it)
        }

        viewModel.messageErrorLiveData.observe(lifecycleOwner) {
            messageAdapter.showError(it)
        }

        fieldsAdapter.onLiveData(viewModel, lifecycleOwner)
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
        messageAdapter.setText(message)
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
        nameAdapter.show(fields)
        emailAdapter.show(fields)
        messageAdapter.show(fields)
        binding.pbLoading.visibility = visibleGone(loading)
        binding.tvSend.visibility = visibleGone(send)
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvOfflineText: TextView = rootView.findViewById(R.id.tv_offline_form_text)
        val email = UsedeskCommonFieldTextAdapter.Binding(rootView.findViewById(R.id.l_offline_form_email), styleValues.getStyle(R.attr.usedesk_chat_screen_offline_form_name))
        val name = UsedeskCommonFieldTextAdapter.Binding(rootView.findViewById(R.id.l_offline_form_name), styleValues.getStyle(R.attr.usedesk_chat_screen_offline_form_email))
        val message = UsedeskCommonFieldTextAdapter.Binding(rootView.findViewById(R.id.l_offline_form_message), styleValues.getStyle(R.attr.usedesk_chat_screen_offline_form_message))
        val rvAdditional: RecyclerView = rootView.findViewById(R.id.rv_offline_form_additional)
        val tvSend: TextView = rootView.findViewById(R.id.tv_offline_form_send)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_offline_form_loading)
        val lAction: ViewGroup = rootView.findViewById(R.id.l_offline_form_send)
    }
}