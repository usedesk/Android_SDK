package ru.usedesk.common_gui

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File

abstract class UsedeskFragment : Fragment() {

    private var filesLauncher: ActivityResultLauncher<String>? = null
    private var cameraLauncher: ActivityResultLauncher<Uri>? = null

    private var cameraFile: File? = null

    open fun onBackPressed(): Boolean = false

    fun registerCamera(onCameraResult: (Boolean) -> Unit) {
        cameraLauncher = cameraLauncher ?: registerForActivityResult(
            ActivityResultContracts.TakePicture(),
            onCameraResult
        )
    }

    fun registerFiles(onContentResult: (List<Uri>) -> Unit) {
        filesLauncher = filesLauncher ?: registerForActivityResult(
            ActivityResultContracts.GetMultipleContents(),
            onContentResult
        )
    }

    fun startFiles() {
        filesLauncher?.launch(MIME_TYPE_ALL_FILES)
    }

    fun startImages() {
        filesLauncher?.launch(MIME_TYPE_ALL_IMAGES)
    }

    fun startCamera() {
        val cameraFile = generateCameraFile()
        this.cameraFile = cameraFile
        val cameraUri = cameraFile.toUri().toProviderUri()
        cameraLauncher?.launch(cameraUri)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        cameraFile?.let {
            outState.putString(CAMERA_FILE_KEY, it.absolutePath)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.getString(CAMERA_FILE_KEY)?.let {
            cameraFile = File(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        cameraLauncher?.unregister()
        cameraLauncher = null

        filesLauncher?.unregister()
        filesLauncher = null
    }

    protected fun generateCameraFile() = File(
        requireContext().cacheDir,
        "camera_${System.currentTimeMillis()}.jpg"
    ).also {
        cameraFile = it
    }

    fun useCameraFile(onCameraFile: (File) -> Unit) {
        cameraFile?.let {
            cameraFile = null
            onCameraFile(it)
        }
    }

    protected fun Uri.toProviderUri() = when (scheme) {
        ContentResolver.SCHEME_FILE -> {
            val applicationContext = requireContext().applicationContext
            FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.provider",
                toFile()
            )
        }
        else -> this
    }

    protected fun argsGetInt(key: String, default: Int): Int {
        return arguments?.getInt(key, default) ?: default
    }

    protected fun argsGetInt(key: String): Int? {
        val args = arguments
        return when {
            args?.containsKey(key) == true -> args.getInt(key)
            else -> null
        }
    }

    protected fun argsGetLong(key: String, default: Long): Long =
        arguments?.getLong(key, default) ?: default

    protected fun argsGetLong(key: String): Long? {
        val args = arguments
        return when {
            args?.containsKey(key) == true -> args.getLong(key)
            else -> null
        }
    }

    protected fun argsGetBoolean(key: String, default: Boolean): Boolean =
        arguments?.getBoolean(key, default) ?: default

    protected fun argsGetBoolean(key: String): Boolean? {
        val args = arguments
        return when {
            args?.containsKey(key) == true -> args.getBoolean(key)
            else -> null
        }
    }

    protected fun argsGetString(key: String): String? = arguments?.getString(key)

    protected fun argsGetString(key: String, default: String): String =
        argsGetString(key) ?: default

    protected fun <T : Parcelable> argsGetParcelable(key: String): T? =
        arguments?.getParcelable(key)

    protected fun <T : Parcelable> argsGetParcelable(key: String, default: T): T =
        argsGetParcelable(key) ?: default

    protected fun argsGetStringArray(key: String): Array<String>? = arguments?.getStringArray(key)

    protected fun argsGetStringArray(key: String, default: Array<String>): Array<String> =
        argsGetStringArray(key) ?: default

    protected fun showSnackbarError(styleValues: UsedeskResourceManager.StyleValues) {
        UsedeskSnackbar.create(
            requireView(),
            styleValues.getColor(R.attr.usedesk_background_color_1),
            styleValues.getString(R.attr.usedesk_text_1),
            styleValues.getColor(R.attr.usedesk_text_color_1)
        ).show()
    }

    inline fun <reified T> findParent(): T? {
        var listener: T? = null

        var parent = parentFragment
        while (parent != null) {
            when (parent) {
                is T -> {
                    listener = parent
                    break
                }
                else -> parent = parent.parentFragment
            }
        }

        return listener ?: activity as? T
    }

    fun <T> Flow<T>.onEachWithOld(action: suspend (old: T?, new: T) -> Unit) = onEachWithOld(
        lifecycleScope,
        action
    )

    protected fun NavController.navigateSafe(
        @IdRes startId: Int,
        @IdRes actionId: Int,
        args: Bundle? = null
    ) {
        if (currentDestination?.id == startId) {
            navigate(actionId, args)
        }
    }

    companion object {
        private const val CAMERA_FILE_KEY = "tempCameraFileKey"
        private const val MIME_TYPE_ALL_IMAGES = "image/*"
        private const val MIME_TYPE_ALL_FILES = "*/*"
    }
}

fun <T> Flow<T>.onEachWithOld(
    scope: CoroutineScope,
    action: suspend (old: T?, new: T) -> Unit
) {
    var oldValue: T? = null
    onEach { new ->
        action(oldValue, new)
        oldValue = new
    }.launchIn(scope)
}