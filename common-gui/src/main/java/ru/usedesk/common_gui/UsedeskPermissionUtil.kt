package ru.usedesk.common_gui

import android.Manifest
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener

object UsedeskPermissionUtil {
    fun needWriteExternalPermission(binding: UsedeskBinding,
                                    errorTitleAttrId: Int,
                                    errorButtonAttrId: Int,
                                    onGranted: () -> Unit) {
        needPermission(binding,
                errorTitleAttrId,
                errorButtonAttrId,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                onGranted)
    }

    fun needReadExternalPermission(binding: UsedeskBinding,
                                   errorTitleAttrId: Int,
                                   errorButtonAttrId: Int,
                                   onGranted: () -> Unit) {
        needPermission(binding,
                errorTitleAttrId,
                errorButtonAttrId,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                onGranted)
    }

    fun needCameraPermission(binding: UsedeskBinding,
                             errorTitleAttrId: Int,
                             errorButtonAttrId: Int,
                             onGranted: () -> Unit) {
        needPermission(binding,
                errorTitleAttrId,
                errorButtonAttrId,
                Manifest.permission.CAMERA, onGranted)
    }

    fun needPermission(binding: UsedeskBinding,
                       errorTitleAttrId: Int,
                       errorButtonAttrId: Int,
                       permission: String?,
                       onGranted: () -> Unit) {
        Dexter.withContext(binding.rootView.context)
                .withPermission(permission)
                .withListener(SnackbarPermissionListener(binding, errorTitleAttrId, errorButtonAttrId, onGranted))
                .check()
    }

    private class SnackbarPermissionListener(binding: UsedeskBinding,
                                             errorTitleAttrId: Int,
                                             errorButtonAttrId: Int,
                                             onGranted: () -> Unit) : PermissionListener {
        private val permissionListener: PermissionListener
        private val onGranted: () -> Unit

        init {
            val errorTitle = binding.styleValues.getString(errorTitleAttrId)
            val errorButton = binding.styleValues.getString(errorButtonAttrId)
            permissionListener = SnackbarOnDeniedPermissionListener.Builder
                    .with(binding.rootView, errorTitle)
                    .withOpenSettingsButton(errorButton).build()
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