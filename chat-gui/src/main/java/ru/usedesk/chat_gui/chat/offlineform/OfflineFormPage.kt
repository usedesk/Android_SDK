
package ru.usedesk.chat_gui.chat.offlineform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.offlineformselector.OfflineFormSelectorPage
import ru.usedesk.chat_gui.chat.requireChatViewModelStoreOwner
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.hideKeyboard
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.visibleGone

internal class OfflineFormPage : UsedeskFragment() {

    private val viewModel: OfflineFormViewModel by viewModels(
        ownerProducer = this@OfflineFormPage::requireChatViewModelStoreOwner
    )

    private lateinit var binding: Binding
    private lateinit var fieldsAdapter: OfflineFormFieldsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateItem(
        inflater,
        container,
        R.layout.usedesk_page_offline_form,
        R.style.Usedesk_Chat_Screen_Offline_Form_Page,
        ::Binding
    ).apply {
        binding = this

        binding.tvSend.setOnClickListener { viewModel.sendClicked() }

        updateActionButton(false)

        init()
    }.rootView

    private fun init() {
        fieldsAdapter = OfflineFormFieldsAdapter(
            binding.rvFields,
            binding,
            viewModel,
            lifecycleScope
        ) { key ->
            findNavController().navigateSafe(
                R.id.dest_offlineFormPage,
                R.id.action_offlineFormPage_to_offlineFormSelectorPage,
                OfflineFormSelectorPage.createBundle(key)
            )
        }

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.offlineFormState != new.offlineFormState) {
                onState(new.offlineFormState)
            }
            if (old?.sendEnabled != new.sendEnabled) {
                updateActionButton(new.sendEnabled)
            }
            if (old?.greetings != new.greetings) {
                binding.tvOfflineText.text = new.greetings
            }
            if (old?.goExit != new.goExit) {
                new.goExit?.use { goToChat ->
                    if (goToChat) {
                        findNavController().navigateSafe(
                            R.id.dest_offlineFormPage,
                            R.id.action_offlineFormPage_to_messagesPage
                        )
                    } else {
                        OfflineFormSuccessDialog.newInstance(binding.rootView).apply {
                            setOnDismissListener {
                                requireActivity().onBackPressed()
                            }
                        }.show()
                    }
                }
            }
            new.hideKeyboard?.use {
                hideKeyboard(binding.rootView)
            }
        }
    }

    private fun onFailed() {
        val sendFailedStyleValues = binding.styleValues
            .getStyleValues(R.attr.usedesk_chat_screen_offline_form_send_failed_snackbar)
        showSnackbarError(sendFailedStyleValues)
    }

    private fun showViews(
        offlineText: Boolean = false,
        fields: Boolean = false,
        loading: Boolean = false,
        send: Boolean = false
    ) {
        binding.tvOfflineText.visibility = visibleGone(offlineText)
        binding.rvFields.visibility = visibleGone(fields)
        binding.pbLoading.visibility = visibleGone(loading)
        binding.tvSend.visibility = visibleGone(send)
    }

    private fun onState(it: OfflineFormViewModel.OfflineFormState) {
        when (it) {
            OfflineFormViewModel.OfflineFormState.DEFAULT -> {
                showViews(
                    offlineText = true,
                    fields = true,
                    send = true
                )
            }
            OfflineFormViewModel.OfflineFormState.SENDING -> {
                showViews(
                    offlineText = true,
                    fields = true,
                    loading = true,
                )
            }
            OfflineFormViewModel.OfflineFormState.FAILED_TO_SEND -> {
                onFailed()
                showViews(
                    offlineText = true,
                    fields = true,
                    send = true
                )
            }
            OfflineFormViewModel.OfflineFormState.SENT_SUCCESSFULLY -> {
                showViews(
                    offlineText = true,
                    fields = true,
                    send = true
                )
            }
        }
    }

    private fun updateActionButton(enabled: Boolean) {
        val attr = if (enabled) {
            binding.tvSend.isEnabled = true
            R.attr.usedesk_chat_screen_offline_form_action_enabled_background
        } else {
            binding.tvSend.isEnabled = false
            R.attr.usedesk_chat_screen_offline_form_action_disabled_background
        }

        val colorId = binding.styleValues.getColor(attr)
        binding.lAction.setBackgroundColor(colorId)
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvOfflineText: TextView = rootView.findViewById(R.id.tv_offline_form_text)
        val rvFields: RecyclerView = rootView.findViewById(R.id.rv_fields)
        val tvSend: TextView = rootView.findViewById(R.id.tv_offline_form_send)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_offline_form_loading)
        val lAction: ViewGroup = rootView.findViewById(R.id.l_offline_form_send)
    }
}