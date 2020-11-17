package ru.usedesk.chat_gui.internal._extra.permission

import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class AppPermissionsListener(
        private val onAccess: () -> Unit,
        private val onDenied: () -> Unit
) : MultiplePermissionsListener {

    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
        if (report.areAllPermissionsGranted()) {
            onAccess()
        } else {
            onDenied()
        }
    }

    override fun onPermissionRationaleShouldBeShown(
            permissions: List<PermissionRequest>,
            token: PermissionToken) {
        token.continuePermissionRequest()
    }
}