package ru.usedesk.chat_gui.chat.offlineform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.*

internal class OfflineFormPage : UsedeskFragment() {

    private val viewModel: OfflineFormViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var fieldsAdapter: OfflineFormFieldsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        if (savedInstanceState == null) {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_page_offline_form,
                    R.style.Usedesk_Chat_Screen) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }

            fieldsAdapter = OfflineFormFieldsAdapter(binding.lAdditional, viewModel) {
                val items = (viewModel.offlineFormSettingsLiveData.value?.topics?.toTypedArray()
                        ?: arrayOf())
                val selectedIndex = viewModel.subjectLiveData.value ?: -1
                getParentListener<IOnOfflineFormSelectorClick>()?.onOfflineFormSelectorClick(items, selectedIndex)
            }

            binding.tvSend.setOnClickListener {
                hideKeyboard(it)
                viewModel.onSendOfflineForm()
            }

            updateActionButton("")
        }

        onLiveData()

        return binding.rootView
    }

    private fun onLiveData() {
        viewModel.offlineFormStateLiveData.observe(viewLifecycleOwner) {
            onState(it)
        }

        fieldsAdapter.onLiveData(viewModel, viewLifecycleOwner)
    }

    fun init() {
        fieldsAdapter.init()
    }

    private fun onSuccessfully() {
        OfflineFormSuccessDialog.newInstance(binding.rootView).apply {
            setOnDismissListener {
                requireActivity().onBackPressed()
            }
        }.show()
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
        fieldsAdapter.show(fields)
        binding.pbLoading.visibility = visibleGone(loading)
        binding.tvSend.visibility = visibleGone(send)
    }

    private fun onState(it: OfflineFormViewModel.OfflineFormState?) {
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
                onSuccessfully()
                showViews(
                        offlineText = true,
                        fields = true,
                        send = true
                )
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

        val colorId = binding.styleValues.getColor(attr)
        binding.lAction.setBackgroundColor(colorId)
    }

    companion object {
        fun newInstance(): OfflineFormPage {
            return OfflineFormPage()
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvOfflineText: TextView = rootView.findViewById(R.id.tv_offline_form_text)
        val lAdditional: ViewGroup = rootView.findViewById(R.id.l_offline_form_additional)
        val tvSend: TextView = rootView.findViewById(R.id.tv_offline_form_send)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_offline_form_loading)
        val lAction: ViewGroup = rootView.findViewById(R.id.l_offline_form_send)
    }
}