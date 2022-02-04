package ru.usedesk.sample.ui.screens.configuration

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputLayout
import ru.usedesk.chat_sdk.UsedeskChatSdk.stopService
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.sample.R
import ru.usedesk.sample.databinding.ScreenConfigurationBinding
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation

class ConfigurationScreen : UsedeskFragment() {

    private val viewModel: ConfigurationViewModel by viewModels()
    private lateinit var binding: ScreenConfigurationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.screen_configuration,
            container, false
        )

        viewModel.configurationLiveData.observe(viewLifecycleOwner) {
            it?.let {
                onNewConfiguration(it)
                viewModel.configurationLiveData.removeObservers(viewLifecycleOwner)
            }
        }
        viewModel.configurationValidation.observe(viewLifecycleOwner) {
            it?.let {
                onNewConfigurationValidation(it)
            }
        }
        viewModel.goToSdkEvent.observe(viewLifecycleOwner) {
            it?.let {
                onGoToSdkEvent(it)
            }
        }
        binding.btnGoToSdk.setOnClickListener {
            onGoToSdk()
        }
        binding.tvServiceType.setOnClickListener {
            PopupMenu(requireContext(), binding.tvServiceType).apply {
                inflate(R.menu.usedesk_service_menu)
                setOnMenuItemClickListener {
                    updateServiceValue(
                        when (it.itemId) {
                            R.id.service_none -> null
                            R.id.service_simple -> false
                            R.id.service_foreground -> true
                            else -> return@setOnMenuItemClickListener false
                        }
                    )
                    stopService(requireContext())
                    true
                }
                show()
            }
        }
        try {
            val version = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
            binding.tvVersion.text = "v$version"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        initTil(binding.tilUrlChat)
        initTil(binding.tilUrlOfflineForm)
        initTil(binding.tilUrlToSendFile)
        initTil(binding.tilUrlApi)
        initTil(binding.tilCompanyId)
        initTil(binding.tilChannelId)
        initTil(binding.tilAccountId)
        initTil(binding.tilToken)
        initTil(binding.tilClientEmail)
        initTil(binding.tilClientPhoneNumber)
        return binding.root
    }

    private fun updateServiceValue(foregroundService: Boolean?) {
        binding.tvServiceType.text = getString(R.string.service_type_title) + ": " + getString(
            when (foregroundService) {
                true -> R.string.service_type_foreground
                false -> R.string.service_type_simple
                null -> R.string.service_type_none
            }
        )
    }

    override fun onPause() {
        super.onPause()
        viewModel.setTempConfiguration(getConfiguration())
    }

    private fun onGoToSdkEvent(event: UsedeskEvent<Any?>) {
        event.process {
            (activity as IOnGoToSdkListener?)?.goToSdk()
        }
    }

    private fun onGoToSdk() {
        viewModel.onGoSdkClick(getConfiguration())
    }

    private fun getConfiguration(): Configuration {
        val additionalFields = mapOf(
            binding.etAdditionalField1Id.text.toString().trim() to
                    binding.etAdditionalField1Value.text.toString().trim(),
            binding.etAdditionalField2Id.text.toString().trim() to
                    binding.etAdditionalField2Value.text.toString().trim(),
            binding.etAdditionalField3Id.text.toString().trim() to
                    binding.etAdditionalField3Value.text.toString().trim()
        ).filter {
            it.key.isNotEmpty()
        }.map {
            it.key.toLong() to it.value
        }.toMap()
        val nestedFields = mapOf(
            binding.etAdditionalNestedField1Id.text.toString().trim() to
                    binding.etAdditionalNestedField1Value.text.toString().trim(),
            binding.etAdditionalNestedField2Id.text.toString().trim() to
                    binding.etAdditionalNestedField2Value.text.toString().trim(),
            binding.etAdditionalNestedField3Id.text.toString().trim() to
                    binding.etAdditionalNestedField3Value.text.toString().trim()
        ).filter {
            it.key.isNotEmpty()
        }.map {
            it.key.toLong() to it.value
        }.toMap()
        val additionalNestedFields = if (nestedFields.isEmpty()) {
            listOf()
        } else {
            listOf(nestedFields)
        }
        return Configuration(
            binding.etUrlChat.text.toString(),
            binding.etUrlOfflineForm.text.toString(),
            binding.etUrlToSendFile.text.toString(),
            binding.etUrlApi.text.toString(),
            binding.etCompanyId.text.toString(),
            binding.etChannelId.text.toString(),
            binding.etAccountId.text.toString(),
            binding.etToken.text.toString(),
            binding.etClientToken.text.toString(),
            binding.etClientEmail.text.toString(),
            binding.etClientName.text.toString(),
            binding.etClientNote.text.toString(),
            binding.etClientPhoneNumber.text.toString().toLongOrNull(),
            binding.etClientAdditionalId.text.toString(),
            binding.etClientInitMessage.text.toString(),
            viewModel.avatarLiveData.value,
            binding.etCustomAgentName.text.toString(),
            when {
                binding.tvServiceType.text.contains(getString(R.string.service_type_foreground)) ->
                    true
                binding.tvServiceType.text.contains(getString(R.string.service_type_simple)) ->
                    false
                else -> null
            },
            binding.switchCacheFiles.isChecked,
            additionalFields,
            additionalNestedFields,
            binding.switchKb.isChecked,
            binding.switchKbWithSupportButton.isChecked,
            binding.switchKbWithArticleRating.isChecked
        )
    }

    private fun onNewConfiguration(configuration: Configuration) {
        binding.etUrlChat.setText(configuration.urlChat)
        binding.etUrlOfflineForm.setText(configuration.urlOfflineForm)
        binding.etUrlToSendFile.setText(configuration.urlToSendFile)
        binding.etUrlApi.setText(configuration.urlApi)
        binding.etCompanyId.setText(configuration.companyId)
        binding.etChannelId.setText(configuration.channelId)
        binding.etAccountId.setText(configuration.accountId)
        binding.etToken.setText(configuration.token)
        binding.etClientToken.setText(configuration.clientToken)
        binding.etClientEmail.setText(configuration.clientEmail)
        binding.etClientName.setText(configuration.clientName)
        binding.etClientPhoneNumber.setText(configuration.clientPhoneNumber?.toString() ?: "")
        binding.etClientAdditionalId.setText(configuration.clientAdditionalId?.toString() ?: "")
        binding.etClientInitMessage.setText(configuration.clientInitMessage)
        binding.etCustomAgentName.setText(configuration.customAgentName)
        updateServiceValue(configuration.foregroundService)
        binding.switchCacheFiles.isChecked = configuration.cacheFiles
        setAdditionalField(
            binding.etAdditionalField1Id,
            binding.etAdditionalField1Value,
            configuration.additionalFields,
            0
        )
        setAdditionalField(
            binding.etAdditionalField2Id,
            binding.etAdditionalField2Value,
            configuration.additionalFields,
            1
        )
        setAdditionalField(
            binding.etAdditionalField3Id,
            binding.etAdditionalField3Value,
            configuration.additionalFields,
            2
        )
        val nested = configuration.additionalNestedFields.firstOrNull() ?: mapOf()
        setAdditionalField(
            binding.etAdditionalNestedField1Id,
            binding.etAdditionalNestedField1Value,
            nested,
            0
        )
        setAdditionalField(
            binding.etAdditionalNestedField2Id,
            binding.etAdditionalNestedField2Value,
            nested,
            1
        )
        setAdditionalField(
            binding.etAdditionalNestedField3Id,
            binding.etAdditionalNestedField3Value,
            nested,
            2
        )
        binding.switchKb.isChecked = configuration.withKb
        binding.switchKbWithSupportButton.isChecked = configuration.withKbSupportButton
        binding.switchKbWithArticleRating.isChecked = configuration.withKbArticleRating
    }

    private fun setAdditionalField(
        etId: EditText,
        etValue: EditText,
        fields: Map<Long, String>,
        index: Int
    ) {
        val id = fields.keys.toList().getOrNull(index)
        if (id != null) {
            val value = fields[id]
            etId.setText(id.toString())
            etValue.setText(value.toString())
        }
    }

    private fun initTil(inputLayout: TextInputLayout) {
        inputLayout.editText?.apply {
            onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                if (hasFocus) {
                    inputLayout.error = null
                }
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    inputLayout.error = null
                }
            })
        }
    }

    private fun showError(
        textInputLayout: TextInputLayout,
        isValid: Boolean,
        errorStringId: Int
    ) {
        textInputLayout.error = if (isValid) {
            null
        } else {
            resources.getString(errorStringId)
        }
    }

    private fun onNewConfigurationValidation(configurationValidation: ConfigurationValidation) {
        configurationValidation.chatConfigurationValidation.run {
            showError(
                binding.tilUrlChat,
                validUrlChat,
                R.string.validation_url_error
            )
            showError(
                binding.tilUrlOfflineForm,
                validUrlOfflineForm,
                R.string.validation_url_error
            )
            showError(
                binding.tilUrlToSendFile,
                validUrlToSendFile,
                R.string.validation_url_error
            )
            showError(
                binding.tilCompanyId,
                validCompanyId,
                R.string.validation_empty_error
            )
            showError(
                binding.tilChannelId,
                validChannelId,
                R.string.validation_empty_error
            )
            showError(
                binding.tilClientEmail,
                validClientEmail,
                R.string.validation_email_error
            )
            showError(
                binding.tilClientPhoneNumber,
                validClientPhoneNumber,
                R.string.validation_phone_error
            )
        }

        configurationValidation.knowledgeBaseConfiguration.run {
            showError(
                binding.tilUrlApi,
                validUrlApi,
                R.string.validation_empty_error
            )
            showError(
                binding.tilAccountId,
                validAccountId,
                R.string.validation_empty_error
            )
            showError(
                binding.tilToken,
                validToken,
                R.string.validation_empty_error
            )
        }
    }

    interface IOnGoToSdkListener {
        fun goToSdk()
    }

    companion object {
        fun newInstance(): ConfigurationScreen {
            return ConfigurationScreen()
        }
    }
}