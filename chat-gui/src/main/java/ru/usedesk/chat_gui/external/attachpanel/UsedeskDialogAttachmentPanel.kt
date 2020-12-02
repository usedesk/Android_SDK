package ru.usedesk.chat_gui.external.attachpanel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.UsedeskDialogAttachmentBinding
import ru.usedesk.chat_gui.external.UsedeskChatFragment
import ru.usedesk.chat_gui.internal._extra.getUsedeskFileInfo
import ru.usedesk.chat_gui.internal._extra.getUsedeskFileInfoList
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.common_gui.external.UsedeskStyleManager
import ru.usedesk.common_gui.internal.PermissionUtil
import ru.usedesk.common_gui.internal.inflateBinding
import java.io.File

class UsedeskDialogAttachmentPanel(
        private val fragment: UsedeskChatFragment
) : BottomSheetDialog(fragment.requireContext(), UsedeskStyleManager.getStyle(R.style.UsedeskAttachmentDialog)) {

    init {
        val container = fragment.view as ViewGroup

        inflateBinding<UsedeskDialogAttachmentBinding>(layoutInflater,
                container,
                R.layout.usedesk_dialog_attachment,
                R.style.Usedesk_Theme_Chat).apply {

            setContentView(root)

            pickPhotoButton.setOnClickListener {
                dismiss()
                PermissionUtil.needReadExternalPermission(container,
                        R.string.need_permission,
                        R.string.settings
                ) {
                    pickImage(fragment)
                }
            }

            takePhotoButton.setOnClickListener {
                dismiss()
                PermissionUtil.needCameraPermission(container,
                        R.string.need_permission,
                        R.string.settings
                ) {
                    takePhoto(fragment)
                }
            }

            pickDocumentButton.setOnClickListener {
                dismiss()
                PermissionUtil.needReadExternalPermission(container,
                        R.string.need_permission,
                        R.string.settings
                ) {
                    pickDocument(fragment)
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val attachedFiles = onResult(fragment.requireContext(), requestCode, data)
            if (attachedFiles != null) {
                fragment.setAttachedFiles(attachedFiles)
            }
        }
    }

    private fun pickFile(fragment: Fragment, mimeType: String) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = mimeType
        }
        fragment.startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
    }

    private fun pickImage(fragment: Fragment) {
        pickFile(fragment, MIME_TYPE_ALL_IMAGES)
    }

    private fun pickDocument(fragment: Fragment) {
        pickFile(fragment, MIME_TYPE_ALL_DOCS)
    }

    private fun takePhoto(fragment: Fragment) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, getTakePhotoUri(fragment.requireContext()))
        }
        fragment.startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO)
    }

    private fun onResult(context: Context, requestCode: Int, data: Intent?): List<UsedeskFileInfo>? {
        when (requestCode) {
            REQUEST_CODE_PICK_FILE -> {
                if (data != null) {
                    return getUsedeskFileInfoList(context, data)
                }
            }
            REQUEST_CODE_TAKE_PHOTO -> {
                return getUsedeskFileInfo(context, getTakePhotoUri(context))
            }
        }
        return null
    }

    private fun getTakePhotoUri(context: Context): Uri {
        return Uri.fromFile(File(context.externalCacheDir, "camera.jpg"))
    }

    companion object {
        private const val REQUEST_CODE_PICK_FILE = 38141
        private const val REQUEST_CODE_TAKE_PHOTO = 38142
        private const val MIME_TYPE_ALL_IMAGES = "image/*"
        private const val MIME_TYPE_ALL_DOCS = "*/*"
    }
}