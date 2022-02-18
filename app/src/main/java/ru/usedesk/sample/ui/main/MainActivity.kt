package ru.usedesk.sample.ui.main

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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

    private var permissionResult: ActivityResultLauncher<String>? = null
    private var onGranted: (() -> Unit)? = null

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
        permissionResult = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onGranted?.invoke()
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
            onGranted = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        permissionResult?.unregister()
        onGranted = null
    }

    private fun needDownloadPermission(
        onGranted: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= 29 ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onGranted()
        } else {
            this.onGranted = onGranted
            permissionResult?.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ?: throw RuntimeException("Need call method register() before.")
        }
    }

    override fun onDownload(url: String, name: String) {
        try {
            needDownloadPermission {//TODO: тут попытка скачать локальный файл рушится
                val downloadManager =
                    getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = Uri.parse(url)
                if (url.startsWith("file://")) {
                    contentResolver.openInputStream(uri).use { inputStream ->
                        if (inputStream == null) {
                            throw UsedeskDataNotFoundException("Can't read file: $url")
                        }
                        val outputPath = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        )
                        val outputFile = File(outputPath, name)
                        FileOutputStream(outputFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                } else {
                    downloadManager.enqueue(
                        DownloadManager.Request(uri)
                            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                            .setTitle(name)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(false)
                            .setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                name
                            )
                    )
                }
                fileToast(R.string.download_started, name)
            }
        } catch (e: Exception) {
            fileToast(R.string.download_failed, name)
        }
    }

    private fun toProviderUri(cameraUri: Uri): Uri {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            cameraUri
        } else {
            val applicationContext = applicationContext
            FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.provider",
                File(cameraUri.path)
            )
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
            configuration.customAgentName,
            REJECTED_FILE_TYPES,
            chatConfiguration
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