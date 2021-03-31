package ru.usedesk.chat_gui.chat.offlineform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.*

internal class OfflineFormPage : UsedeskFragment() {

    private val viewModel: OfflineFormViewModel by viewModels()

    private var rootView: View? = null
    private lateinit var binding: Binding
    private lateinit var fieldsAdapter: OfflineFormFieldsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        if (rootView == null) {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_page_offline_form,
                    R.style.Usedesk_Chat_Screen) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }
            rootView = binding.rootView

            binding.tvSend.setOnClickListener {
                hideKeyboard(it)
                viewModel.onSendOfflineForm { goToChat ->
                    if (goToChat) {
                        getParentListener<IOnGoToChatListener>()?.onGoToMessages()
                    } else {
                        OfflineFormSuccessDialog.newInstance(binding.rootView).apply {
                            setOnDismissListener {
                                requireActivity().onBackPressed()
                            }
                        }.show()
                    }
                }
            }

            fieldsAdapter = OfflineFormFieldsAdapter(binding.rvFields, binding, viewModel) { items, selectedIndex ->
                getParentListener<IOnOfflineFormSelectorClick>()?.onOfflineFormSelectorClick(items, selectedIndex)
            }

            updateActionButton(false)

            init()
        }

        onLiveData()

        return binding.rootView
    }

    private fun onLiveData() {
        fieldsAdapter.onLiveData(viewModel, viewLifecycleOwner)

        viewModel.offlineFormStateLiveData.observe(viewLifecycleOwner) {
            it?.let {
                onState(it)
            }
        }

        viewModel.sendEnabledLiveData.observe(viewLifecycleOwner) {
            updateActionButton(it == true)
        }

        viewModel.offlineFormSettings.observe(viewLifecycleOwner) {
            binding.tvOfflineText.text = it?.callbackGreeting
        }
    }

    private fun init() {
        viewModel.init(
                binding.styleValues.getString(R.attr.usedesk_chat_screen_offline_form_name),
                binding.styleValues.getString(R.attr.usedesk_chat_screen_offline_form_email),
                binding.styleValues.getString(R.attr.usedesk_chat_screen_offline_form_message),
        )
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

    fun setSubjectIndex(index: Int) {
        viewModel.onSubjectIndexChanged(index)
    }

    companion object {
        fun newInstance(): OfflineFormPage {
            return OfflineFormPage()
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvOfflineText: TextView = rootView.findViewById(R.id.tv_offline_form_text)
        val rvFields: RecyclerView = rootView.findViewById(R.id.rv_fields)
        val tvSend: TextView = rootView.findViewById(R.id.tv_offline_form_send)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_offline_form_loading)
        val lAction: ViewGroup = rootView.findViewById(R.id.l_offline_form_send)
    }
}