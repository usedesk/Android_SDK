package ru.usedesk.chat_gui.internal._extra.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import ru.usedesk.chat_gui.R

fun needWriteExternalPermission(binding: ViewDataBinding, onAccessed: () -> Unit) {
    needPermissions(binding, onAccessed, Manifest.permission.WRITE_EXTERNAL_STORAGE)
}

fun needReadExternalPermission(binding: ViewDataBinding, onAccessed: () -> Unit) {
    needPermissions(binding, onAccessed, Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun needPermissions(binding: ViewDataBinding,
                    onAccessed: () -> Unit,
                    vararg permissions: String) {
    Dexter.withContext(binding.root.context)
            .withPermissions(*permissions)
            .withListener(AppPermissionsListener({
                onAccessed()
            }, {
                showDeniedSnackbar(binding, permissions.size)
            })).check()
}

private fun showDeniedSnackbar(binding: ViewDataBinding,
                               permissionCount: Int) {
    val stringId = if (permissionCount > 1) {
        R.string.need_permissions
    } else {
        R.string.need_permission
    }
    Snackbar.make(binding.root, stringId, BaseTransientBottomBar.LENGTH_SHORT)
            .setAction(R.string.settings) {
                val uri = Uri.fromParts("package",
                        it.context.packageName,
                        null)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(uri)
                binding.root.context.startActivity(intent)
            }
            .show()
}