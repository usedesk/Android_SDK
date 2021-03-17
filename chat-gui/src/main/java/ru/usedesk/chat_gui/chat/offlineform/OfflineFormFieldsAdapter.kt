package ru.usedesk.chat_gui.chat.offlineform

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.*

internal class OfflineFormFieldsAdapter(
        private val rootView: ViewGroup,
        private val viewModel: OfflineFormViewModel,
        private val onSubjectClick: () -> Unit
) : IUsedeskAdapter<OfflineFormViewModel> {

    private val nameAdapter: UsedeskCommonFieldTextAdapter
    private val emailAdapter: UsedeskCommonFieldTextAdapter
    private val subjectAdapter: UsedeskCommonFieldListAdapter
    private val additionalAdapters = mutableListOf<UsedeskCommonFieldTextAdapter>()
    private val messageAdapter: UsedeskCommonFieldTextAdapter

    init {
        val nameBinding = inflateItem(rootView,
                R.layout.usedesk_item_field_text,
                R.style.Usedesk_Chat_Screen_Offline_Form_Name) { rootView, defaultStyleId ->
            UsedeskCommonFieldTextAdapter.Binding(rootView, defaultStyleId)
        }
        nameAdapter = UsedeskCommonFieldTextAdapter(nameBinding).apply {
            setText(viewModel.configuration.clientName)
            setTextChangeListener {
                viewModel.onOfflineFormNameChanged(it)
            }
        }
        rootView.addView(nameBinding.rootView)

        val emailBinding = inflateItem(rootView,
                R.layout.usedesk_item_field_text,
                R.style.Usedesk_Chat_Screen_Offline_Form_Email) { rootView, defaultStyleId ->
            UsedeskCommonFieldTextAdapter.Binding(rootView, defaultStyleId)
        }
        emailAdapter = UsedeskCommonFieldTextAdapter(emailBinding).apply {
            setText(viewModel.configuration.clientEmail)
            setTextChangeListener {
                viewModel.onOfflineFormEmailChanged(it)
            }
        }
        rootView.addView(emailBinding.rootView)

        val subjectBinding = inflateItem(rootView,
                R.layout.usedesk_item_field_list,
                R.style.Usedesk_Chat_Screen_Offline_Form_Additional) { rootView, defaultStyleId ->
            UsedeskCommonFieldListAdapter.Binding(rootView, defaultStyleId)
        }
        subjectAdapter = UsedeskCommonFieldListAdapter(subjectBinding) {
            onSubjectClick()
        }
        rootView.addView(subjectBinding.rootView)

        val messageBinding = inflateItem(rootView,
                R.layout.usedesk_item_field_text,
                R.style.Usedesk_Chat_Screen_Offline_Form_Message) { rootView, defaultStyleId ->
            UsedeskCommonFieldTextAdapter.Binding(rootView, defaultStyleId)
        }
        messageAdapter = UsedeskCommonFieldTextAdapter(messageBinding).apply {
            setTextChangeListener {
                viewModel.onOfflineFormMessageChanged(it)
            }
        }
        rootView.addView(messageBinding.rootView)
    }

    fun init() {
        showKeyboard(messageAdapter.binding.etText)
    }

    override fun onLiveData(viewModel: OfflineFormViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.offlineFormSettingsLiveData.observe(lifecycleOwner) {
            it.customFields.forEachIndexed { index, customField ->
                val additionalBinding = inflateItem(rootView,
                        R.layout.usedesk_item_field_text,
                        R.style.Usedesk_Chat_Screen_Offline_Form_Additional) { rootView, defaultStyleId ->
                    UsedeskCommonFieldTextAdapter.Binding(rootView, defaultStyleId)
                }
                additionalAdapters.add(UsedeskCommonFieldTextAdapter(additionalBinding).apply {
                    setTitle(customField.placeholder, customField.required)
                })
                rootView.addView(additionalBinding.rootView, index + 3)
            }
        }

        viewModel.nameErrorLiveData.observe(lifecycleOwner) {
            nameAdapter.showError(it)
        }

        viewModel.emailErrorLiveData.observe(lifecycleOwner) {
            emailAdapter.showError(it)
        }

        viewModel.subjectLiveData.observe(lifecycleOwner) { index ->
            viewModel.offlineFormSettingsLiveData.value?.let {
                subjectAdapter.setTitle(it.topics[index], it.topicsRequired)
            }
        }

        viewModel.messageErrorLiveData.observe(lifecycleOwner) {
            messageAdapter.showError(it)
        }
    }

    fun show(show: Boolean) {
        rootView.visibility = visibleGone(show)
    }
}