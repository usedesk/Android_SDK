package ru.usedesk.chat_gui.chat.offlineform

import androidx.lifecycle.LifecycleOwner
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.common_gui.*

internal class OfflineFormFieldsAdapter(
        private val binding: OfflineFormPage.Binding,
        private val viewModel: OfflineFormViewModel,
        private val onSubjectClick: () -> Unit
) : IUsedeskAdapter<OfflineFormViewModel> {

    private val nameAdapter: UsedeskCommonFieldTextAdapter
    private val emailAdapter: UsedeskCommonFieldTextAdapter
    private val subjectAdapter: UsedeskCommonFieldListAdapter
    private val additionalAdapter = OfflineFormAdditionalAdapter()
    private val messageAdapter: UsedeskCommonFieldTextAdapter

    private var settings: UsedeskOfflineFormSettings? = null

    init {
        val nameBinding = inflateItem(rootView,
                R.layout.usedesk_item_field_text,
                R.style.Usedesk_Chat_Screen_Offline_Form_Name) { rootView, defaultStyleId ->
            UsedeskCommonFieldTextAdapter.Binding(rootView, defaultStyleId)
        }
        nameAdapter = UsedeskCommonFieldTextAdapter(nameBinding)
        rootView.addView(nameBinding.rootView)

        val emailBinding = inflateItem(rootView,
                R.layout.usedesk_item_field_text,
                R.style.Usedesk_Chat_Screen_Offline_Form_Email) { rootView, defaultStyleId ->
            UsedeskCommonFieldTextAdapter.Binding(rootView, defaultStyleId)
        }
        emailAdapter = UsedeskCommonFieldTextAdapter(emailBinding)
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
        messageAdapter = UsedeskCommonFieldTextAdapter(messageBinding)
        rootView.addView(messageBinding.rootView)
    }

    fun init(settings: UsedeskOfflineFormSettings) {
        if (this.settings == null) {
            this.settings = settings
            showKeyboard(messageAdapter.binding.etText)

            subjectUpdate(viewModel.subjectLiveData.value ?: -1)

            additionalAdapter.init(settings.customFields)
        }

        nameAdapter.apply {
            setText(viewModel.configuration.clientName)
            setTextChangeListener {
                viewModel.onOfflineFormNameChanged(it)
            }
        }

        emailAdapter.apply {
            setText(viewModel.configuration.clientEmail)
            setTextChangeListener {
                viewModel.onOfflineFormEmailChanged(it)
            }
        }

        messageAdapter.apply {
            setTextChangeListener {
                viewModel.onOfflineFormMessageChanged(it)
            }
        }

        /*val additionals = viewModel.additionalsLiveData.value
        additionalAdapters.forEachIndexed { index, adapter ->
            val customField = settings.customFields[index]
            adapter.setTitle(customField.placeholder, customField.required)
            adapter.setText(additionals?.get(index))
        }*/
    }

    override fun onLiveData(viewModel: OfflineFormViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.nameErrorLiveData.observe(lifecycleOwner) {
            nameAdapter.showError(it)
        }

        viewModel.emailErrorLiveData.observe(lifecycleOwner) {
            emailAdapter.showError(it)
        }

        viewModel.subjectLiveData.observe(lifecycleOwner) { index ->
            subjectUpdate(index ?: -1)
        }

        viewModel.messageErrorLiveData.observe(lifecycleOwner) {
            messageAdapter.showError(it)
        }
    }

    private fun subjectUpdate(index: Int) {
        settings?.run {
            val title = topicsTitle ?: ""
            subjectAdapter.setTitle(title, topicsRequired)
            if (index >= 0) {
                val text = topics.getOrNull(index) ?: ""
                subjectAdapter.setText(text)
            }
        }
    }

    fun show(show: Boolean) {
        rootView.visibility = visibleGone(show)
    }
}