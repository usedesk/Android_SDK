package ru.usedesk.common_gui

import android.Manifest
import android.view.View
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener

object UsedeskPermissionUtil {
    fun needWriteExternalPermission(view: View,
                                    errorTitleId: Int,
                                    errorButtonId: Int,
                                    onGranted: () -> Unit) {
        needPermission(view,
                errorTitleId,
                errorButtonId,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                onGranted)
    }

    fun needReadExternalPermission(view: View,
                                   errorTitleId: Int,
                                   errorButtonId: Int,
                                   onGranted: () -> Unit) {
        needPermission(view,
                errorTitleId,
                errorButtonId,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                onGranted)
    }

    fun needCameraPermission(view: View,
                             errorTitleId: Int,
                             errorButtonId: Int,
                             onGranted: () -> Unit) {
        needPermission(view,
                errorTitleId,
                errorButtonId,
                Manifest.permission.CAMERA, onGranted)
    }

    fun needPermission(view: View,
                       errorTitleId: Int,
                       errorButtonId: Int,
                       permission: String?,
                       onGranted: () -> Unit) {
        Dexter.withContext(view.context)
                .withPermission(permission)
                .withListener(SnackbarPermissionListener(view, errorTitleId, errorButtonId, onGranted))
                .check()
    }

    private class SnackbarPermissionListener(view: View,
                                             errorTitleId: Int,
                                             errorButtonId: Int,
                                             onGranted: () -> Unit) : PermissionListener {
        private val permissionListener: PermissionListener
        private val onGranted: () -> Unit

        init {
            permissionListener = SnackbarOnDeniedPermissionListener.Builder
                    .with(view, errorTitleId)
                    .withOpenSettingsButton(errorButtonId).build()
            this.onGranted = onGranted
        }

        override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
            permissionListener.onPermissionGranted(permissionGrantedResponse)
            onGranted()
        }

        override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
            permissionListener.onPermissionDenied(permissionDeniedResponse)
        }

        override fun onPermissionRationaleShouldBeShown(permissionRequest: PermissionRequest,
                                                        permissionToken: PermissionToken) {
            permissionListener.onPermissionRationaleShouldBeShown(permissionRequest, permissionToken)
        }
    }
}