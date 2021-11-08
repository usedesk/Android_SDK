package ru.usedesk.sample.ui.main

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ru.usedesk.chat_gui.IUsedeskOnClientTokenListener
import ru.usedesk.chat_gui.IUsedeskOnFileClickListener
import ru.usedesk.chat_sdk.UsedeskChatSdk.setNotificationsServiceFactory
import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.knowledgebase_gui.screens.IUsedeskOnSupportClickListener
import ru.usedesk.sample.R
import ru.usedesk.sample.databinding.ActivityMainBinding
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.service.CustomForegroundNotificationsService
import ru.usedesk.sample.service.CustomSimpleNotificationsService
import ru.usedesk.sample.ui.screens.configuration.ConfigurationScreen.IOnGoToSdkListener

class MainActivity : AppCompatActivity(),
    IOnGoToSdkListener,
    IUsedeskOnSupportClickListener,
    IUsedeskOnFileClickListener,
    IUsedeskOnClientTokenListener {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        StrictMode.setVmPolicy(VmPolicy.Builder().build())

        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        viewModel.configurationLiveData.observe(this, {
            it?.let {
                onConfiguration(it)
            }
        })
        viewModel.errorLiveData.observe(this, {
            it?.let {
                onError(it)
            }
        })
        viewModel.init(
            MainNavigation(this, R.id.container),
            supportFragmentManager.backStackEntryCount == 0
        )
    }

    private fun onError(error: UsedeskEvent<String>) {
        error.process { text: String? ->
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun onConfiguration(configuration: Configuration) {
        initUsedeskService(configuration)
    }

    private fun initUsedeskService(configuration: Configuration) {
        if (configuration.foregroundService) {
            CustomForegroundNotificationsService.Factory()
        } else {
            CustomSimpleNotificationsService.Factory()
        }.let { factory ->
            setNotificationsServiceFactory(factory)
        }
    }

    private fun getCurrentFragment(): Fragment? =
        supportFragmentManager.findFragmentById(R.id.container)

    override fun onFileClick(usedeskFile: UsedeskFile) {
        viewModel.goShowFile(usedeskFile)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    override fun onSupportClick() {
        viewModel.goChat()
    }

    override fun goToSdk() {
        viewModel.goSdk()
    }

    override fun onClientToken(clientToken: String) {
        viewModel.onClientToken(clientToken)
    }
}