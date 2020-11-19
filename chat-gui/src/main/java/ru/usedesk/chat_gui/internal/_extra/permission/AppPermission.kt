package ru.usedesk.chat_gui.internal._extra.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import ru.usedesk.chat_gui.R

fun needWriteExternalPermission(view: View, onAccessed: () -> Unit) {
    needPermissions(view, onAccessed, Manifest.permission.WRITE_EXTERNAL_STORAGE)
}

fun needReadExternalPermission(view: View, onAccessed: () -> Unit) {
    needPermissions(view, onAccessed, Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun needPermissions(view: View,
                    onAccessed: () -> Unit,
                    vararg permissions: String) {
    Dexter.withContext(view.context)
            .withPermissions(*permissions)
            .withListener(AppPermissionsListener({
                onAccessed()
            }, {
                showDeniedSnackbar(view, permissions.size)
            })).check()
}

private fun showDeniedSnackbar(view: View,
                               permissionCount: Int) {
    val stringId = if (permissionCount > 1) {
        R.string.need_permissions
    } else {
        R.string.need_permission
    }
    Snackbar.make(view, stringId, BaseTransientBottomBar.LENGTH_SHORT)
            .setAction(R.string.settings) {
                val uri = Uri.fromParts("package",
                        it.context.packageName,
                        null)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(uri)
                view.context.startActivity(intent)
            }
            .show()
}