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
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputLayout
import ru.usedesk.chat_sdk.UsedeskChatSdk
import ru.usedesk.chat_sdk.UsedeskChatSdk.stopService
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.showInstead
import ru.usedesk.sample.GlideApp
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
            inflater,
            R.layout.screen_configuration,
            container,
            false
        )

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.avatar != new.avatar) {
                GlideApp.with(binding.ivAvatar)
                    .load(new.avatar)
                    .into(binding.ivAvatar)
            }
            if (old?.configuration != new.configuration) {
                new.configuration.onNewConfiguration()
            }
            if (old?.validation != new.validation) {
                new.validation?.onNewConfigurationValidation()
            }
            if (old?.clientToken != new.clientToken) {
                showInstead(binding.pbCreateChat, binding.btnCreateChat, new.clientToken.loading)
                new.clientToken.completed?.use {
                    binding.etClientToken.setText(it)
                    Toast.makeText(
                        requireContext(),
                        "Success:$it",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                new.clientToken.error?.use {
                    Toast.makeText(
                        requireContext(),
                        "Failed:$it",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        binding.btnGoToSdk.setOnClickListener {
            val configuration = getConfiguration()
            if (viewModel.onGoSdkClick(configuration)) {
                (activity as IOnGoToSdkListener?)?.goToSdk(configuration)
            }
        }
        binding.btnCreateChat.setOnClickListener {
            val configuration = getConfiguration()
            if (viewModel.onCreateChat(configuration)) {
                val preparation = UsedeskChatSdk.initPreparation(
                    requireContext(),
                    configuration.toChatConfiguration()
                )
                viewModel.createChat(
                    preparation,
                    configuration.token
                )
            }
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
        binding.switchMaterialComponents.setOnCheckedChangeListener { _, _ ->
            if (viewModel.isMaterialComponentsSwitched(getConfiguration())) {
                requireActivity().finish()
                startActivity(requireActivity().intent)
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
        initTil(binding.tilUrlApi)
        initTil(binding.tilCompanyId)
        initTil(binding.tilChannelId)
        initTil(binding.tilAccountId)
        initTil(binding.tilToken)
        initTil(binding.tilClientEmail)
        initTil(binding.tilClientPhoneNumber)
        initTil(binding.tilMessagesPageSize)
        binding.ivAvatar.setOnClickListener {
            startImages()
        }
        registerFiles {
            it.firstOrNull()?.let { uri ->
                val url = uri.toString()
                binding.ivAvatar.tag = url
                viewModel.setAvatar(url)
            }
        }
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

    private fun getConfiguration(): Configuration {
        val additionalFields = mapOf(
            binding.etAdditionalField1Id.text.toString().trim() to
                    binding.etAdditionalField1Value.text.toString().trim(),
            binding.etAdditionalField2Id.text.toString().trim() to
                    binding.etAdditionalField2Value.text.toString().trim(),
            binding.etAdditionalField3Id.text.toString().trim() to
                    binding.etAdditionalField3Value.text.toString().trim()
        ).filter { it.key.isNotEmpty() }
            .map { it.key.toLong() to it.value }
            .toMap()
        val nestedFields = mapOf(
            binding.etAdditionalNestedField1Id.text.toString().trim() to
                    binding.etAdditionalNestedField1Value.text.toString().trim(),
            binding.etAdditionalNestedField2Id.text.toString().trim() to
                    binding.etAdditionalNestedField2Value.text.toString().trim(),
            binding.etAdditionalNestedField3Id.text.toString().trim() to
                    binding.etAdditionalNestedField3Value.text.toString().trim()
        ).filter { it.key.isNotEmpty() }
            .map { it.key.toLong() to it.value }
            .toMap()
        val additionalNestedFields = when {
            nestedFields.isEmpty() -> listOf()
            else -> listOf(nestedFields)
        }
        return Configuration(
            binding.switchMaterialComponents.isChecked,
            binding.etUrlChat.text.toString(),
            binding.etUrlOfflineForm.text.toString(),
            binding.etUrlApi.text.toString(),
            binding.etCompanyId.text.toString(),
            binding.etChannelId.text.toString(),
            binding.etAccountId.text.toString(),
            binding.etMessagesPageSize.text.toString().toIntOrNull() ?: 1,
            binding.etToken.text.toString(),
            binding.etClientToken.text.toString(),
            binding.etClientEmail.text.toString(),
            binding.etClientName.text.toString(),
            binding.etClientNote.text.toString(),
            binding.etClientPhoneNumber.text.toString().toLongOrNull(),
            binding.etClientAdditionalId.text.toString(),
            binding.etClientInitMessage.text.toString(),
            binding.ivAvatar.tag as? String,
            binding.etCustomAgentName.text.toString(),
            binding.etCustomDateFormat.text.toString(),
            binding.etCustomTimeFormat.text.toString(),
            when {
                binding.tvServiceType.text.contains(getString(R.string.service_type_foreground)) -> true
                binding.tvServiceType.text.contains(getString(R.string.service_type_simple)) -> false
                else -> null
            },
            binding.switchCacheFiles.isChecked,
            binding.switchGroupAgentMessages.isChecked,
            binding.adaptiveTimePadding.isChecked,
            additionalFields,
            additionalNestedFields,
            binding.switchKb.isChecked,
            binding.switchKbWithSupportButton.isChecked,
            binding.switchKbWithArticleRating.isChecked
        )
    }

    private fun Configuration.onNewConfiguration() {
        binding.switchMaterialComponents.isChecked = materialComponents
        binding.etUrlChat.setText(urlChat)
        binding.etUrlOfflineForm.setText(urlChatApi)
        binding.etUrlApi.setText(urlApi)
        binding.etCompanyId.setText(companyId)
        binding.etChannelId.setText(channelId)
        binding.etMessagesPageSize.setText(messagesPageSize.toString())
        binding.etAccountId.setText(accountId)
        binding.etToken.setText(token)
        binding.etClientToken.setText(clientToken)
        binding.etClientEmail.setText(clientEmail)
        binding.etClientName.setText(clientName)
        binding.etClientPhoneNumber.setText(clientPhoneNumber?.toString() ?: "")
        binding.etClientAdditionalId.setText(clientAdditionalId ?: "")
        binding.etClientInitMessage.setText(clientInitMessage)
        binding.etCustomAgentName.setText(customAgentName)
        binding.etCustomDateFormat.setText(messagesDateFormat)
        binding.etCustomTimeFormat.setText(messageTimeFormat)
        updateServiceValue(foregroundService)
        binding.switchCacheFiles.isChecked = cacheFiles
        binding.switchGroupAgentMessages.isChecked = groupAgentMessages
        binding.adaptiveTimePadding.isChecked = adaptiveTimePadding
        setAdditionalField(
            binding.etAdditionalField1Id,
            binding.etAdditionalField1Value,
            additionalFields,
            0
        )
        setAdditionalField(
            binding.etAdditionalField2Id,
            binding.etAdditionalField2Value,
            additionalFields,
            1
        )
        setAdditionalField(
            binding.etAdditionalField3Id,
            binding.etAdditionalField3Value,
            additionalFields,
            2
        )
        val nested = additionalNestedFields.firstOrNull() ?: mapOf()
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
        binding.switchKb.isChecked = withKb
        binding.switchKbWithSupportButton.isChecked = withKbSupportButton
        binding.switchKbWithArticleRating.isChecked = withKbArticleRating
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
        textInputLayout.error = when {
            isValid -> null
            else -> resources.getString(errorStringId)
        }
    }

    private fun ConfigurationValidation.onNewConfigurationValidation() {
        chatConfigurationValidation.run {
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

        knowledgeBaseConfiguration.run {
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
        fun goToSdk(configuration: Configuration)
    }
}