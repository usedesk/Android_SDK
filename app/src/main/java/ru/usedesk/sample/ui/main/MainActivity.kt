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
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import ru.usedesk.chat_gui.IUsedeskOnClientTokenListener
import ru.usedesk.chat_gui.IUsedeskOnDownloadListener
import ru.usedesk.chat_gui.IUsedeskOnFileClickListener
import ru.usedesk.chat_gui.IUsedeskOnFullscreenListener
import ru.usedesk.chat_gui.chat.UsedeskChatScreen
import ru.usedesk.chat_gui.showfile.UsedeskShowFileScreen
import ru.usedesk.chat_sdk.UsedeskChatSdk.setNotificationsServiceFactory
import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.knowledgebase_gui.screens.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.screens.main.UsedeskKnowledgeBaseScreen
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
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        StrictMode.setVmPolicy(VmPolicy.Builder().build())

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHostFragment.navController

        viewModel.configurationLiveData.initAndObserve(this) {
            initUsedeskService(it)
        }
        viewModel.errorLiveData.observe(this) {
            it?.let {
                onError(it)
            }
        }
        viewModel.goSdkEventLiveData.observe(this) { event ->
            event?.process {
                val configuration = viewModel.configurationLiveData.value
                if (configuration.withKb) {
                    val kbConfiguration = configuration.toKbConfiguration()
                    navController.navigate(
                        R.id.action_configurationScreen_to_usedeskKnowledgeBaseScreen,
                        UsedeskKnowledgeBaseScreen.createBundle(
                            configuration.withKbSupportButton,
                            configuration.withKbArticleRating,
                            kbConfiguration
                        )
                    )
                } else {
                    val chatConfiguration = configuration.toChatConfiguration()
                    navController.navigate(
                        R.id.action_configurationScreen_to_usedeskChatScreen,
                        UsedeskChatScreen.createBundle(
                            configuration.customAgentName,
                            REJECTED_FILE_TYPES,
                            chatConfiguration
                        )
                    )
                }
            }
        }
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

    private fun initUsedeskService(configuration: Configuration) {
        when (configuration.foregroundService) {
            true -> {
                CustomForegroundNotificationsService.Factory()
            }
            false -> {
                CustomSimpleNotificationsService.Factory()
            }
            else -> {
                null
            }
        }.let { factory ->
            setNotificationsServiceFactory(factory)
        }
    }

    override fun getFullscreenLayout() = binding.lFullscreen

    override fun onFullscreenChanged(fullscreen: Boolean) {
        fullscreenMode(fullscreen)
    }

    override fun onFileClick(usedeskFile: UsedeskFile) {
        navController.navigate(
            R.id.action_usedeskChatScreen_to_usedeskShowFileScreen,
            UsedeskShowFileScreen.createBundle(usedeskFile)
        )
    }

    override fun onBackPressed() {
        val currentFragment = navHostFragment.childFragmentManager.fragments.getOrNull(0)
                as? UsedeskFragment
        if (currentFragment?.onBackPressed() != true && !navController.popBackStack()) {
            super.onBackPressed()
        }
    }

    override fun onSupportClick() {
        val configuration = viewModel.configurationLiveData.value
        val chatConfiguration = configuration.toChatConfiguration()
        navController.navigate(
            R.id.action_usedeskKnowledgeBaseScreen_to_usedeskChatScreen,
            UsedeskChatScreen.createBundle(
                configuration.customAgentName,
                REJECTED_FILE_TYPES,
                chatConfiguration
            )
        )
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

    companion object {
        private val REJECTED_FILE_TYPES = listOf("apk", "jar", "dex", "so", "aab")
    }
}