package ru.usedesk.sample.ui.screens.configuration

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputLayout
import ru.usedesk.chat_sdk.UsedeskChatSdk.stopService
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.sample.R
import ru.usedesk.sample.databinding.ScreenConfigurationBinding
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.model.configuration.entity.ConfigurationValidation

class ConfigurationScreen : Fragment() {

    private val viewModel: ConfigurationViewModel by viewModels()
    private lateinit var binding: ScreenConfigurationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.screen_configuration,
                container, false)

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
        binding.switchForeground.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            stopService(requireContext())
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

    private fun getConfiguration(): Configuration = Configuration(
            binding.etUrlChat.text.toString(),
            binding.etUrlOfflineForm.text.toString(),
            binding.etUrlToSendFile.text.toString(),
            binding.etUrlApi.text.toString(),
            binding.etCompanyId.text.toString(),
            binding.etChannelId.text.toString(),
            binding.etAccountId.text.toString(),
            binding.etToken.text.toString(),
            binding.etClientSignature.text.toString(),
            binding.etClientEmail.text.toString(),
            binding.etClientName.text.toString(),
            binding.etClientNote.text.toString(),
            binding.etClientPhoneNumber.text.toString().toLongOrNull(),
            binding.etClientAdditionalId.text.toString().toLongOrNull(),
            binding.etClientInitMessage.text.toString(),
            binding.etCustomAgentName.text.toString(),
            binding.switchForeground.isChecked,
            binding.switchKb.isChecked,
            binding.switchKbWithSupportButton.isChecked,
            binding.switchKbWithArticleRating.isChecked)


    private fun onNewConfiguration(configuration: Configuration) {
        binding.etUrlChat.setText(configuration.urlChat)
        binding.etUrlOfflineForm.setText(configuration.urlOfflineForm)
        binding.etUrlToSendFile.setText(configuration.urlToSendFile)
        binding.etUrlApi.setText(configuration.urlApi)
        binding.etCompanyId.setText(configuration.companyId)
        binding.etChannelId.setText(configuration.channelId)
        binding.etAccountId.setText(configuration.accountId)
        binding.etToken.setText(configuration.token)
        binding.etClientSignature.setText(configuration.clientSignature)
        binding.etClientEmail.setText(configuration.clientEmail)
        binding.etClientName.setText(configuration.clientName)
        binding.etClientPhoneNumber.setText(configuration.clientPhoneNumber?.toString() ?: "")
        binding.etClientAdditionalId.setText(configuration.clientAdditionalId?.toString() ?: "")
        binding.etClientInitMessage.setText(configuration.clientInitMessage)
        binding.etCustomAgentName.setText(configuration.customAgentName)
        binding.switchForeground.isChecked = configuration.foregroundService
        binding.switchKb.isChecked = configuration.withKb
        binding.switchKbWithSupportButton.isChecked = configuration.withKbSupportButton
        binding.switchKbWithArticleRating.isChecked = configuration.withKbArticleRating
    }

    private fun initTil(inputLayout: TextInputLayout) {
        inputLayout.editText?.apply {
            onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
                if (hasFocus) {
                    inputLayout.error = null
                }
            }
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    inputLayout.error = null
                }
            })
        }
    }

    private fun showError(textInputLayout: TextInputLayout,
                          isValid: Boolean,
                          errorStringId: Int) {
        textInputLayout.error = if (isValid) {
            null
        } else {
            resources.getString(errorStringId)
        }
    }

    private fun onNewConfigurationValidation(configurationValidation: ConfigurationValidation) {
        configurationValidation.chatConfigurationValidation.run {
            showError(binding.tilUrlChat,
                    validUrlChat,
                    R.string.validation_url_error)
            showError(binding.tilUrlOfflineForm,
                    validUrlOfflineForm,
                    R.string.validation_url_error)
            showError(binding.tilUrlToSendFile,
                    validUrlToSendFile,
                    R.string.validation_url_error)
            showError(binding.tilCompanyId,
                    validCompanyId,
                    R.string.validation_empty_error)
            showError(binding.tilChannelId,
                    validChannelId,
                    R.string.validation_empty_error)
            showError(binding.tilClientEmail,
                    validClientEmail,
                    R.string.validation_email_error)
            showError(binding.tilClientPhoneNumber,
                    validClientPhoneNumber,
                    R.string.validation_phone_error)
        }

        configurationValidation.knowledgeBaseConfiguration.run {
            showError(binding.tilUrlApi,
                    validUrlApi,
                    R.string.validation_empty_error)
            showError(binding.tilAccountId,
                    validAccountId,
                    R.string.validation_empty_error)
            showError(binding.tilToken,
                    validToken,
                    R.string.validation_empty_error)
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