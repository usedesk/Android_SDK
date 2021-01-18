package ru.usedesk.common_gui

import android.Manifest
import androidx.fragment.app.Fragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

object UsedeskPermissionUtil {
    fun needWriteExternalPermission(binding: UsedeskBinding,
                                    fragment: Fragment,
                                    onGranted: () -> Unit) {
        needPermission(binding,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                fragment,
                onGranted)
    }

    fun needReadExternalPermission(binding: UsedeskBinding,
                                   fragment: Fragment,
                                   onGranted: () -> Unit) {
        needPermission(binding,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                fragment,
                onGranted)
    }

    fun needCameraPermission(binding: UsedeskBinding,
                             fragment: Fragment,
                             onGranted: () -> Unit) {
        needPermission(binding,
                Manifest.permission.CAMERA,
                fragment,
                onGranted)
    }

    fun needPermission(binding: UsedeskBinding,
                       permission: String?,
                       fragment: Fragment,
                       onGranted: () -> Unit) {
        Dexter.withContext(binding.rootView.context)
                .withPermission(permission)
                .withListener(SnackbarPermissionListener(binding, fragment, onGranted))
                .check()
    }

    private class SnackbarPermissionListener(
            private val binding: UsedeskBinding,
            private val fragment: Fragment,
            private val onGranted: () -> Unit
    ) : PermissionListener {

        override fun onPermissionGranted(permissionGrantedResponse: PermissionGrantedResponse) {
            onGranted()
        }

        override fun onPermissionDenied(permissionDeniedResponse: PermissionDeniedResponse) {
            binding.styleValues.apply {
                UsedeskSnackbar.create(
                        fragment,
                        getColor(R.attr.usedesk_common_permission_needed_background_color),
                        getString(R.attr.usedesk_common_permission_needed_message_text),
                        getColor(R.attr.usedesk_common_permission_needed_message_color),
                        getString(R.attr.usedesk_common_permission_needed_action_text),
                        getColor(R.attr.usedesk_common_permission_needed_action_color)
                ).show()
            }
        }

        override fun onPermissionRationaleShouldBeShown(permissionRequest: PermissionRequest,
                                                        permissionToken: PermissionToken) {
            permissionToken.continuePermissionRequest()
        }
    }
}