
package ru.usedesk.common_gui

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

internal class PermissionLauncher(
    private val fragment: UsedeskFragment,
    private val permission: String,
    private val onGranted: () -> Unit
) {
    private val permissionLauncher: ActivityResultLauncher<String>

    private var dialog: AlertDialog? = null
    private val titleText: String?
    private val messageText: String?
    private val positiveText: String?

    private var dialogIsShown: Boolean = false

    private val dialogStyleId = UsedeskResourceManager.getResourceId(
        R.style.Usedesk_Common_Alert_Dialog_Camera
    )

    init {
        val dialogStyle = UsedeskResourceManager.getStyleValues(
            fragment.requireContext(),
            dialogStyleId
        )
        titleText = dialogStyle.findString(R.attr.usedesk_text_1)
        messageText = dialogStyle.findString(R.attr.usedesk_text_2)
        positiveText = dialogStyle.findString(R.attr.usedesk_text_3)

        permissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onGranted()
            } else {
                showNoPermissions()
            }
        }
    }

    private fun showNoPermissions() {
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

    fun launch() {
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED &&
            fragment.shouldShowRequestPermissionRationale(permission)
        ) {
            dialogIsShown = true
            showDescriptionDialog()
        } else {
            launchPermission()
        }
    }

    fun unregister() {
        permissionLauncher.unregister()

        dialog?.run {
            setOnDismissListener(null)
            dismiss()
        }
        dialog = null
    }

    fun save(outState: Bundle) {
        outState.putBoolean(DIALOG_IS_SHOWN_KEY, dialogIsShown)
    }

    fun load(savedInstanceState: Bundle?) {
        if (savedInstanceState?.getBoolean(DIALOG_IS_SHOWN_KEY) == true) {
            showDescriptionDialog()
        }
    }

    private fun showDescriptionDialog() {
        if (titleText != null && messageText != null) {
            dialogIsShown = true
            (dialog ?: createDescriptionDialog()).show()
        } else {
            launchPermission()
        }
    }

    private fun createDescriptionDialog(): AlertDialog = AlertDialog.Builder(
        fragment.requireContext(),
        dialogStyleId
    ).setTitle(
        titleText
    ).setMessage(
        messageText
    ).setPositiveButton(positiveText) { _, _ ->
        launchPermission()
    }.setOnDismissListener {
        dialogIsShown = false
    }.create().also {
        dialog = it
    }

    private fun launchPermission() {
        permissionLauncher.launch(permission)
    }

    companion object {
        private const val DIALOG_IS_SHOWN_KEY = "dialogIsShownKey"
    }
}