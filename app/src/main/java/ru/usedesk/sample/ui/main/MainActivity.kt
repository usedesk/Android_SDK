
package ru.usedesk.sample.ui.main

import android.Manifest
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
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
import ru.usedesk.common_gui.onEachWithOld
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.knowledgebase_gui.screen.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.screen.IUsedeskOnWebUrlListener
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseScreen
import ru.usedesk.knowledgebase_gui.screen.UsedeskKnowledgeBaseScreen.DeepLink
import ru.usedesk.sample.R
import ru.usedesk.sample.databinding.ActivityMainBinding
import ru.usedesk.sample.model.configuration.entity.Configuration
import ru.usedesk.sample.service.CustomForegroundNotificationsService
import ru.usedesk.sample.ui.screens.configuration.ConfigurationScreen.IOnGoToSdkListener
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity(),
    IOnGoToSdkListener,
    IUsedeskOnSupportClickListener,
    IUsedeskOnFileClickListener,
    IUsedeskOnClientTokenListener,
    IUsedeskOnDownloadListener,
    IUsedeskOnFullscreenListener,
    IUsedeskOnWebUrlListener {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    private var permissionDownloadResult: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val materialComponents = viewModel.modelFlow.value.configuration.common.materialComponents
        when {
            materialComponents -> mapOf(
                R.style.Usedesk_Chat_Screen_Messages_Page to R.style.Chat_Screen_Messages_Page_MaterialComponents,
                R.style.Usedesk_Chat_Screen_Offline_Form_Page to R.style.Chat_Screen_Offline_Form_Page_MaterialComponents
            )
            else -> listOf(
                R.style.Usedesk_Chat_Screen_Messages_Page,
                R.style.Usedesk_Chat_Screen_Offline_Form_Page
            ).associateWith { it }
        }.forEach {
            UsedeskResourceManager.replaceResourceId(it.key, it.value)
        }
        val themeId = when {
            materialComponents -> R.style.AppTheme_MaterialComponents
            else -> R.style.AppTheme
        }
        setTheme(themeId)
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        StrictMode.setVmPolicy(VmPolicy.Builder().build())

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        navHostFragment = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHostFragment.navController

        viewModel.modelFlow.onEachWithOld(lifecycleScope) { old, new ->
            if (old?.configuration != new.configuration) {
                initUsedeskService(new.configuration)
            }
            if (old?.error != new.error) {
                new.error?.onError()
            }
            if (old?.goSdk != new.goSdk) {
                new.goSdk?.use {
                    navController.apply {
                        if (new.configuration.kb.withKb) {
                            val kbConfiguration = new.configuration.toKbConfiguration()
                            val kb = new.configuration.kb
                            val deepLink = when {
                                kb.article && kb.articleId != null -> DeepLink.Article(
                                    articleId = kb.articleId,
                                    noBackStack = kb.noBackStack
                                )
                                kb.category && kb.categoryId != null -> DeepLink.Category(
                                    categoryId = kb.categoryId,
                                    noBackStack = kb.noBackStack
                                )
                                kb.section && kb.sectionId != null -> DeepLink.Section(
                                    sectionId = kb.sectionId,
                                    noBackStack = kb.noBackStack
                                )
                                else -> null
                            }
                            navigateSafe(
                                R.id.dest_configurationScreen,
                                R.id.action_configurationScreen_to_usedeskKnowledgeBaseScreen,
                                UsedeskKnowledgeBaseScreen.createBundle(
                                    configuration = kbConfiguration,
                                    withSupportButton = kb.withKbSupportButton,
                                    deepLink = deepLink
                                )
                            )
                        } else {
                            navigateSafe(
                                R.id.dest_configurationScreen,
                                R.id.action_configurationScreen_to_usedeskChatScreen,
                                createChatScreenBundle(new.configuration)
                            )
                        }
                    }
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
                    R.style.Usedesk_Common_No_Permission_Snackbar
                )
                UsedeskResourceManager.StyleValues(
                    this,
                    snackbarStyleId
                ).apply {
                    UsedeskSnackbar.create(
                        binding.root,
                        getColor(R.attr.usedesk_background_color_1),
                        getString(R.attr.usedesk_text_1),
                        getColor(R.attr.usedesk_text_color_1),
                        getString(R.attr.usedesk_text_2),
                        getColor(R.attr.usedesk_text_color_2)
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
                when (uri.scheme) {
                    ContentResolver.SCHEME_FILE,
                    ContentResolver.SCHEME_CONTENT -> contentResolver.openInputStream(uri)
                        .use { inputStream ->
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
                            FileOutputStream(outputFile).use(inputStream::copyTo)
                        }
                    else -> downloadManager.enqueue(
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

    override fun onWebUrl(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(browserIntent)
    }

    override fun onDownload(url: String, name: String) {
        viewModel.setDownloadFile(MainViewModel.DownloadFile(url, name))
        permissionDownloadResult?.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun fileToast(descriptionId: Int, name: String) {
        val description = resources.getString(descriptionId)
        Toast.makeText(this, "$description:\n${name}", Toast.LENGTH_SHORT).show()
    }

    private fun UsedeskEvent<String>.onError() = use { text: String? ->
        Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
    }

    private fun initUsedeskService(configuration: Configuration) {
        setNotificationsServiceFactory(
            when {
                configuration.chat.foregroundService -> CustomForegroundNotificationsService.Factory()
                else -> null
            }
        )
    }

    override fun getFullscreenLayout() = binding.lFullscreen

    override fun onFullscreenChanged(fullscreen: Boolean) = fullscreenMode(fullscreen)

    override fun onFileClick(usedeskFile: UsedeskFile) {
        navController.navigateSafe(
            R.id.dest_usedeskChatScreen,
            R.id.action_usedeskChatScreen_to_usedeskShowFileScreen,
            UsedeskShowFileScreen.createBundle(usedeskFile)
        )
    }

    private inline fun <reified T> getChildFragment(): T? = navHostFragment
        .childFragmentManager
        .fragments
        .filterIsInstance<T>()
        .firstOrNull()

    override fun onBackPressed() {
        val currentFragment = getChildFragment<UsedeskFragment>()
        if (currentFragment?.onBackPressed() != true && !navController.popBackStack()) {
            super.onBackPressed()
        }
    }

    override fun onSupportClick() {
        navController.navigateSafe(
            R.id.dest_usedeskKnowledgeBaseScreen,
            R.id.action_usedeskKnowledgeBaseScreen_to_usedeskChatScreen,
            createChatScreenBundle(viewModel.modelFlow.value.configuration)
        )
    }

    private fun createChatScreenBundle(configuration: Configuration): Bundle {
        val chatConfiguration = configuration.toChatConfiguration()
        if (configuration.chat.adaptiveTimePadding) {
            mapOf(
                R.style.Usedesk_Chat_Message_Text_Agent to R.style.Custom_Chat_Message_Text_Agent,
                R.style.Usedesk_Chat_Message_Text_Client to R.style.Custom_Chat_Message_Text_Client
            )
        } else {
            mapOf(
                R.style.Usedesk_Chat_Message_Text_Agent to R.style.Usedesk_Chat_Message_Text_Agent,
                R.style.Usedesk_Chat_Message_Text_Client to R.style.Usedesk_Chat_Message_Text_Client
            )
        }.forEach {
            UsedeskResourceManager.replaceResourceId(it.key, it.value)
        }
        return UsedeskChatScreen.createBundle(
            chatConfiguration,
            configuration.chat.customAgentName.ifEmpty { null },
            REJECTED_FILE_TYPES,
            messagesDateFormat = configuration.chat.messagesDateFormat.ifEmpty { null },
            messageTimeFormat = configuration.chat.messageTimeFormat.ifEmpty { null },
            groupAgentMessages = configuration.chat.groupAgentMessages,
            adaptiveTextMessageTimePadding = configuration.chat.adaptiveTimePadding
        )
    }

    override fun goToSdk(configuration: Configuration) = viewModel.goSdk(configuration)

    override fun onClientToken(clientToken: String) = viewModel.onClientToken(clientToken)

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

fun NavController.navigateSafe(
    @IdRes startId: Int,
    @IdRes actionId: Int,
    args: Bundle? = null
) {
    if (currentDestination?.id == startId) {
        navigate(actionId, args)
    }
}