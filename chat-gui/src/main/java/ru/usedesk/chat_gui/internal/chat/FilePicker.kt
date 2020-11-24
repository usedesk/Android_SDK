package ru.usedesk.chat_gui.internal.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import ru.usedesk.chat_gui.internal.utils.AttachmentUtils
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import java.io.File

class FilePicker {
    private fun pickFile(fragment: Fragment, mimeType: String) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = mimeType
        fragment.startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
    }

    fun pickImage(fragment: Fragment) {
        pickFile(fragment, MIME_TYPE_ALL_IMAGES)
    }

    fun pickDocument(fragment: Fragment) {
        pickFile(fragment, MIME_TYPE_ALL_DOCS)
    }

    fun takePhoto(fragment: Fragment) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTakePhotoUri(fragment.context!!))
        fragment.startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO)
    }

    fun onResult(context: Context, requestCode: Int, data: Intent?): List<UsedeskFileInfo>? {
        when (requestCode) {
            REQUEST_CODE_PICK_FILE -> {
                if (data != null) {
                    return AttachmentUtils.getUsedeskFileInfoList(context, data)
                }
            }
            REQUEST_CODE_TAKE_PHOTO -> {
                return AttachmentUtils.getUsedeskFileInfo(context, getTakePhotoUri(context))
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