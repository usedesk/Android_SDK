package ru.usedesk.common_gui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File

abstract class UsedeskFragment : Fragment() {

    private var permissionResult: ActivityResultLauncher<String>? = null
    private var onGranted: (() -> Unit)? = null
    private var getContent: ActivityResultLauncher<String>? = null
    private var fromCamera: ActivityResultLauncher<Uri>? = null

    open fun onBackPressed(): Boolean = false

    fun registerPermissions() {
        permissionResult = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onGranted?.invoke()
            } else {
                val snackbarStyleId = UsedeskResourceManager.getResourceId(
                    R.style.Usedesk_Common_No_Permission_Snackbar
                )
                UsedeskResourceManager.StyleValues(
                    requireContext(),
                    snackbarStyleId
                ).apply {
                    UsedeskSnackbar.create(
                        requireView(),
                        getColor(R.attr.usedesk_background_color_1),
                        getString(R.attr.usedesk_text_1),
                        getColor(R.attr.usedesk_text_color_1),
                        getString(R.attr.usedesk_text_2),
                        getColor(R.attr.usedesk_text_color_2)
                    ).show()
                }
            }
            onGranted = null
        }
    }

    fun registerFiles(
        onFiles: (List<Uri>) -> Unit,
        onCamera: (Boolean) -> Unit
    ) {
        getContent =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
                onFiles(uriList)
            }

        fromCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            onCamera(it)
        }
    }

    fun unregister() {
        permissionResult?.unregister()
        permissionResult = null

        getContent?.unregister()
        getContent = null

        fromCamera?.unregister()
        fromCamera = null
    }

    fun needWriteExternalPermission(
        fragment: Fragment,
        onGranted: () -> Unit
    ) {
        needPermission(
            fragment,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            onGranted
        )
    }

    fun needReadExternalPermission(
        fragment: Fragment,
        onGranted: () -> Unit
    ) {
        needPermission(
            fragment,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            onGranted
        )
    }

    fun needCameraPermission(
        fragment: Fragment,
        onGranted: () -> Unit
    ) {
        needPermission(
            fragment,
            Manifest.permission.CAMERA,
            onGranted
        )
    }

    fun needPermission(
        fragment: Fragment,
        permission: String,
        onGranted: () -> Unit
    ) {
        when (ContextCompat.checkSelfPermission(fragment.requireContext(), permission)) {
            PackageManager.PERMISSION_GRANTED -> onGranted()
            PackageManager.PERMISSION_DENIED -> {
                this.onGranted = onGranted
                permissionResult?.launch(permission)
                    ?: throw RuntimeException("Need call method register() before.")
            }
        }
    }

    fun fromGallery() {
        getContent?.launch(MIME_TYPE_ALL_IMAGES)
    }

    fun fromStorage() {
        getContent?.launch(MIME_TYPE_ALL_DOCS)
    }

    fun fromCamera(uri: Uri) {
        val photoUri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri
        } else {
            val applicationContext = requireContext().applicationContext
            FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.provider",
                File(uri.path ?: "")
            )
        }
        fromCamera?.launch(photoUri)
    }

    protected fun argsGetInt(key: String, default: Int): Int {
        return arguments?.getInt(key, default) ?: default
    }

    protected fun argsGetInt(key: String): Int? {
        val args = arguments
        return if (args?.containsKey(key) == true) {
            args.getInt(key)
        } else {
            null
        }
    }

    protected fun argsGetLong(key: String, default: Long): Long {
        return arguments?.getLong(key, default) ?: default
    }

    protected fun argsGetLong(key: String): Long? {
        val args = arguments
        return if (args?.containsKey(key) == true) {
            args.getLong(key)
        } else {
            null
        }
    }

    protected fun argsGetBoolean(key: String, default: Boolean): Boolean {
        return arguments?.getBoolean(key, default) ?: default
    }

    protected fun argsGetBoolean(key: String): Boolean? {
        val args = arguments
        return if (args?.containsKey(key) == true) {
            args.getBoolean(key)
        } else {
            null
        }
    }

    protected fun argsGetString(key: String): String? {
        return arguments?.getString(key)
    }

    protected fun argsGetString(key: String, default: String): String {
        return argsGetString(key) ?: default
    }

    protected fun <T : Parcelable> argsGetParcelable(key: String): T? {
        return arguments?.getParcelable(key)
    }

    protected fun <T : Parcelable> argsGetParcelable(key: String, default: T): T {
        return argsGetParcelable(key) ?: default
    }

    protected fun argsGetStringArray(key: String): Array<String>? {
        return arguments?.getStringArray(key)
    }

    protected fun argsGetStringArray(key: String, default: Array<String>): Array<String> {
        return argsGetStringArray(key) ?: default
    }

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
            if (parent is T) {
                listener = parent
                break
            } else {
                parent = parent.parentFragment
            }
        }

        return listener ?: activity as? T
    }

    companion object {
        private const val MIME_TYPE_ALL_IMAGES = "*/*"
        private const val MIME_TYPE_ALL_DOCS = "*/*"
    }
}