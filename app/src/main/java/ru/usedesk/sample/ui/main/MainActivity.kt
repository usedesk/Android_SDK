package ru.usedesk.sample.ui.main

import android.app.DownloadManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import ru.usedesk.chat_gui.IUsedeskOnClientTokenListener
import ru.usedesk.chat_gui.IUsedeskOnDownloadListener
import ru.usedesk.chat_gui.IUsedeskOnFileClickListener
import ru.usedesk.chat_gui.IUsedeskOnFullscreenListener
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
    IUsedeskOnClientTokenListener,
    IUsedeskOnDownloadListener,
    IUsedeskOnFullscreenListener {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        StrictMode.setVmPolicy(VmPolicy.Builder().build())

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

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

    override fun onDownload(url: String, name: String) {
        try {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(url))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setTitle(name)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
            val downloadID = downloadManager.enqueue(request)
            fileToast(R.string.download_started, name)
        } catch (e: Exception) {
            fileToast(R.string.download_failed, name)
        }
    }

    private fun fileToast(descriptionId: Int, name: String) {
        val description = resources.getString(descriptionId)
        Toast.makeText(this, "$description:\n${name}", Toast.LENGTH_SHORT).show()
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

    override fun getFullscreenLayout() = binding.lFullscreen

    override fun onFullscreenChanged(fullscreen: Boolean) {
        fullscreenMode(fullscreen)
    }

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

    private fun fullscreenMode(enable: Boolean) {
        if (enable) {
            val fullscreenFlags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            window.decorView.run {
                systemUiVisibility = fullscreenFlags
                setOnSystemUiVisibilityChangeListener {
                    if (android.R.attr.visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                        systemUiVisibility = fullscreenFlags
                    }
                }
            }

            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        } else {
            val windowedFlags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

            window.decorView.run {
                systemUiVisibility = windowedFlags
                setOnSystemUiVisibilityChangeListener(null)
            }

            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        }
    }
}