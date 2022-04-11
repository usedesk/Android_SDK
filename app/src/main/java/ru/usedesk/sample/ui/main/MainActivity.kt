package ru.usedesk.sample.ui.main

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.UsedeskSnackbar
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.knowledgebase_gui.screens.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.screens.main.UsedeskKnowledgeBaseScreen
import ru.usedesk.sample.R
import ru.usedesk.sample.databinding.ActivityMainBinding
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.service.CustomForegroundNotificationsService
import ru.usedesk.sample.service.CustomSimpleNotificationsService
import ru.usedesk.sample.ui.screens.configuration.ConfigurationScreen.IOnGoToSdkListener
import java.io.File
import java.io.FileOutputStream

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

    private var permissionDownloadResult: ActivityResultLauncher<String>? = null

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
                    navController.navigate(
                        R.id.action_configurationScreen_to_usedeskChatScreen,
                        createChatScreenBundle(configuration)
                    )
                }
            }
        }
        permissionDownloadResult = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                downloadFile()
            } else {
                val snackbarStyleId = UsedeskResourceManager.getResourceId(
                    ru.usedesk.common_gui.R.style.Usedesk_Common_No_Permission_Snackbar
                )
                UsedeskResourceManager.StyleValues(
                    this,
                    snackbarStyleId
                ).apply {
                    UsedeskSnackbar.create(
                        binding.root,
                        getColor(ru.usedesk.common_gui.R.attr.usedesk_background_color_1),
                        getString(ru.usedesk.common_gui.R.attr.usedesk_text_1),
                        getColor(ru.usedesk.common_gui.R.attr.usedesk_text_color_1),
                        getString(ru.usedesk.common_gui.R.attr.usedesk_text_2),
                        getColor(ru.usedesk.common_gui.R.attr.usedesk_text_color_2)
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        permissionDownloadResult?.unregister()
        permissionDownloadResult = null
    }

    private fun downloadFile() {
        viewModel.useDownloadFile { downloadFile ->
            try {
                val downloadManager =
                    getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = Uri.parse(downloadFile.url)
                if (uri.scheme == "file" || uri.scheme == "content") {
                    contentResolver.openInputStream(uri).use { inputStream ->
                        if (inputStream == null) {
                            throw UsedeskDataNotFoundException("Can't read file: ${downloadFile.url}")
                        }
                        val outputPath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        )
                        var outputFile = File(outputPath, downloadFile.name)
                        val fileName = outputFile.nameWithoutExtension
                        val fileExtension = outputFile.extension
                        var count = 0
                        while (outputFile.exists()) {
                            count++
                            var name = "$fileName $count"
                            if (name.length - fileExtension.length > 254) {
                                name = "${fileName.hashCode()} $count"
                            }
                            if (fileExtension.isNotEmpty()) {
                                name = "$name.${outputFile.extension}"
                            }

                            outputFile = File(outputPath, name)
                        }
                        FileOutputStream(outputFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                } else {
                    downloadManager.enqueue(
                        DownloadManager.Request(uri)
                            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            .setTitle(downloadFile.name)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(false)
                            .setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                downloadFile.name
                            )
                    )
                }
                fileToast(R.string.download_started, downloadFile.name)
            } catch (e: Exception) {
                fileToast(R.string.download_failed, downloadFile.name)
            }
        }
    }

    override fun onDownload(url: String, name: String) {
        viewModel.setDownloadFile(MainViewModel.DownloadFile(url, name))
        permissionDownloadResult?.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
            true -> CustomForegroundNotificationsService.Factory()
            false -> CustomSimpleNotificationsService.Factory()
            else -> null
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
        navController.navigate(
            R.id.action_usedeskKnowledgeBaseScreen_to_usedeskChatScreen,
            createChatScreenBundle(viewModel.configurationLiveData.value)
        )
    }

    private fun createChatScreenBundle(configuration: Configuration): Bundle {
        val chatConfiguration = configuration.toChatConfiguration()
        return UsedeskChatScreen.createBundle(
            configuration.customAgentName.ifEmpty { null },
            REJECTED_FILE_TYPES,
            chatConfiguration,
            messagesDateFormat = configuration.messagesDateFormat.ifEmpty { null },
            messageTimeFormat = configuration.messageTimeFormat.ifEmpty { null },
            groupAgentMessages = configuration.groupAgentMessages
        )
    }

    override fun goToSdk() {
        viewModel.goSdk()
    }

    override fun onClientToken(clientToken: String) {
        viewModel.onClientToken(clientToken)
    }

    private fun fullscreenMode(enable: Boolean) {
        ViewCompat.getWindowInsetsController(binding.root)?.run {
            systemBarsBehavior = if (enable) {
                hide(WindowInsetsCompat.Type.systemBars())
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                show(WindowInsetsCompat.Type.systemBars())
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
            }
        }
    }

    companion object {
        private val REJECTED_FILE_TYPES = listOf("apk", "jar", "dex", "so", "aab")
    }
}