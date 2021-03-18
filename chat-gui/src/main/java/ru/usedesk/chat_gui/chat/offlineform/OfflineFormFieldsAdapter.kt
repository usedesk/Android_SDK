package ru.usedesk.chat_gui.chat.offlineform

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
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
                R.style.Usedesk_Chat_Screen_Offline_Form_Subject) { rootView, defaultStyleId ->
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
            subjectUpdate(it, viewModel.subjectLiveData.value ?: -1)
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
            subjectUpdate(viewModel.offlineFormSettingsLiveData.value, index ?: -1)
        }

        viewModel.messageErrorLiveData.observe(lifecycleOwner) {
            messageAdapter.showError(it)
        }
    }

    private fun subjectUpdate(offlineFormSettings: UsedeskOfflineFormSettings?, index: Int) {
        offlineFormSettings?.let {
            val title = it.topicsTitle ?: ""
            subjectAdapter.setTitle(title, it.topicsRequired)
            if (index >= 0) {
                val text = it.topics.getOrNull(index) ?: ""
                subjectAdapter.setText(text)
            }
        }
    }

    fun show(show: Boolean) {
        rootView.visibility = visibleGone(show)
    }
}