package ru.usedesk.sample.ui.screens.configuration

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import ru.usedesk.chat_sdk.UsedeskChatSdk.stopService
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.sample.R
import ru.usedesk.sample.databinding.ScreenConfigurationBinding
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation

class ConfigurationScreen : Fragment() {

    private val viewModel: ConfigurationViewModel by viewModels()
    private lateinit var binding: ScreenConfigurationBinding

    private lateinit var getContent: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getContent =
            requireActivity().registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    onAvatar(uri.toString())
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.screen_configuration,
            container, false
        )

        viewModel.configurationLiveData.observe(viewLifecycleOwner, {
            it?.let {
                onNewConfiguration(it)
                viewModel.configurationLiveData.removeObservers(viewLifecycleOwner)
            }
        })
        viewModel.configurationValidation.observe(viewLifecycleOwner, {
            it?.let {
                onNewConfigurationValidation(it)
            }
        })
        viewModel.goToSdkEvent.observe(viewLifecycleOwner, {
            it?.let {
                onGoToSdkEvent(it)
            }
        })
        binding.btnGoToSdk.setOnClickListener {
            onGoToSdk()
        }
        binding.switchForeground.setOnCheckedChangeListener { _, _ ->
            stopService(requireContext())
        }
        try {
            val version = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
            binding.tvVersion.text = "v$version"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        binding.ivClientAvatar.setOnClickListener {
            Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        getContent.launch("image/*")
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(requireContext(), "Need permissions", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {
                    }
                }).check()
        }
        binding.ivClientAvatarReset.setOnClickListener {
            onAvatar(null)
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
            binding.etClientAdditionalId.text.toString().toLongOrNull(),
            binding.etClientInitMessage.text.toString(),
            viewModel.avatarLiveData.value,
            binding.etCustomAgentName.text.toString(),
            binding.switchForeground.isChecked,
            binding.switchCacheFiles.isChecked,
            additionalFields,
            additionalNestedFields,
            binding.switchKb.isChecked,
            binding.switchKbWithSupportButton.isChecked,
            binding.switchKbWithArticleRating.isChecked
        )
    }

    private fun onAvatar(avatar: String?) {
        viewModel.setAvatar(avatar)
        when (avatar) {
            null -> {
                binding.ivClientAvatar.setImageResource(R.drawable.usedesk_background_avatar_def)
                binding.tvClientAvatar.text = "Do not change client avatar"
            }
            "" -> {
                binding.ivClientAvatar.setImageResource(R.drawable.usedesk_background_avatar_def)
                binding.tvClientAvatar.text = "Reset client avatar"
            }
            else -> {
                Glide.with(requireContext())
                    .load(avatar)
                    .into(binding.ivClientAvatar)
                binding.tvClientAvatar.text = "Change client avatar"
            }
        }
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
        binding.switchForeground.isChecked = configuration.foregroundService
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
        onAvatar(configuration.clientAvatar)
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