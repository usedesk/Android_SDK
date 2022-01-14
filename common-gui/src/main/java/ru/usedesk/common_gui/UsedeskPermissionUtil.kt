package ru.usedesk.common_gui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

object UsedeskPermissionUtil {
    private var permissionResult: ActivityResultLauncher<String>? = null
    private var onGranted: (() -> Unit)? = null

    fun register(fragment: Fragment) {
        permissionResult = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onGranted?.invoke()
            } else {
                val snackbarStyleId = UsedeskResourceManager.getResourceId(
                    R.style.Usedesk_Common_No_Permission_Snackbar
                )
                UsedeskResourceManager.StyleValues(
                    fragment.requireContext(),
                    snackbarStyleId
                ).apply {
                    UsedeskSnackbar.create(
                        fragment.requireView(),
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

    fun release() {
        permissionResult?.unregister()
    }

    fun needWriteExternalPermission(
        fragment: Fragment,
        onGranted: () -> Unit
    ) {
        needWriteExternalPermission(
            fragment.requireActivity(),
            onGranted
        )
    }

    fun needReadExternalPermission(
        fragment: Fragment,
        onGranted: () -> Unit
    ) {
        needReadExternalPermission(
            fragment.requireActivity(),
            onGranted
        )
    }

    fun needCameraPermission(
        fragment: Fragment,
        onGranted: () -> Unit
    ) {
        needCameraPermission(
            fragment.requireActivity(),
            onGranted
        )
    }

    fun needWriteExternalPermission(
        activity: FragmentActivity,
        onGranted: () -> Unit
    ) {
        needPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            onGranted
        )
    }

    fun needReadExternalPermission(
        activity: FragmentActivity,
        onGranted: () -> Unit
    ) {
        needPermission(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            onGranted
        )
    }

    fun needCameraPermission(
        activity: FragmentActivity,
        onGranted: () -> Unit
    ) {
        needPermission(
            activity,
            Manifest.permission.CAMERA,
            onGranted
        )
    }

    fun needPermission(
        activity: FragmentActivity,
        permission: String,
        onGranted: () -> Unit
    ) {
        when (ContextCompat.checkSelfPermission(activity, permission)) {
            PackageManager.PERMISSION_GRANTED -> onGranted()
            PackageManager.PERMISSION_DENIED -> {
                this.onGranted = onGranted
                permissionResult?.launch(permission)
                    ?: throw RuntimeException("Need call method register() before.")
            }
        }
    }
}