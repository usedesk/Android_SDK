package ru.usedesk.chat_gui.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskPermissionUtil
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.inflateItem
import java.io.File

internal class UsedeskAttachmentDialog private constructor(
        private val screen: UsedeskChatScreen,
        dialogStyle: Int
) : BottomSheetDialog(screen.requireContext(), dialogStyle) {

    private val binding: Binding

    init {
        val container = screen.view as ViewGroup

        binding = inflateItem(layoutInflater,
                container,
                R.layout.usedesk_dialog_attachment,
                dialogStyle) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        setContentView(binding.rootView)

        binding.lGallery.setOnClickListener {
            dismiss()
            UsedeskPermissionUtil.needReadExternalPermission(binding, screen) {
                pickImage(screen)
            }
        }

        binding.lCamera.setOnClickListener {
            dismiss()
            UsedeskPermissionUtil.needCameraPermission(binding, screen) {
                takePhoto(screen)
            }
        }
        binding.lStorage.setOnClickListener {
            dismiss()
            UsedeskPermissionUtil.needReadExternalPermission(binding, screen) {
                pickDocument(screen)
            }
        }

        BottomSheetBehavior.from(binding.rootView.parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val attachedFiles = onResult(screen.requireContext(), requestCode, data)
            if (attachedFiles != null) {
                screen.setAttachedFiles(attachedFiles)
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
        val applicationContext = fragment.requireContext().applicationContext
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            val uri = getTakePhotoUri(fragment.requireContext())
            val photoUri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                uri
            } else {
                FileProvider.getUriForFile(
                        applicationContext,
                        "${applicationContext.packageName}.provider",
                        File(uri.path ?: "")
                )
            }
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (cameraIntent.resolveActivity(applicationContext.packageManager) != null) {
            fragment.startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO)
        }
    }

    private fun onResult(context: Context, requestCode: Int, data: Intent?): List<UsedeskFileInfo>? {
        when (requestCode) {
            REQUEST_CODE_PICK_FILE -> {
                if (data != null) {
                    return getUsedeskFileInfoList(context, data)
                }
            }
            REQUEST_CODE_TAKE_PHOTO -> {
                cameraFileUri?.also {
                    return getUsedeskFileInfo(context, it)
                }
            }
        }
        return null
    }

    private fun getTakePhotoUri(context: Context): Uri {
        val fileName = "camera_${System.currentTimeMillis()}.jpg"
        return Uri.fromFile(File(context.externalCacheDir, fileName)).apply {
            cameraFileUri = this
        }
    }

    private fun getUriList(data: Intent): List<Uri> {
        val uri = data.data //single file
        val clipData = data.clipData //list of files
        if (clipData != null) {
            return (0 until clipData.itemCount).mapNotNull { i ->
                clipData.getItemAt(i).uri
            }
        } else if (uri != null) {
            return listOf(uri)
        }
        return listOf()
    }

    private fun getUsedeskFileInfoList(context: Context, data: Intent): List<UsedeskFileInfo> {
        return getUriList(data).map {
            UsedeskFileInfo.create(context, it)
        }
    }

    private fun getUsedeskFileInfo(context: Context, uri: Uri): List<UsedeskFileInfo> {
        return listOf(UsedeskFileInfo.create(context, uri))
    }

    companion object {
        private const val REQUEST_CODE_PICK_FILE = 38141
        private const val REQUEST_CODE_TAKE_PHOTO = 38142
        private const val MIME_TYPE_ALL_IMAGES = "image/*"
        private const val MIME_TYPE_ALL_DOCS = "*/*"

        private var cameraFileUri: Uri? = null

        fun create(screen: UsedeskChatScreen): UsedeskAttachmentDialog {
            val dialogStyle = UsedeskResourceManager.getResourceId(
                    R.style.Usedesk_Chat_Attachment_Dialog
            )
            return UsedeskAttachmentDialog(screen, dialogStyle)
        }
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val lGallery: View = rootView.findViewById(R.id.l_gallery)
        val lCamera: View = rootView.findViewById(R.id.l_camera)
        val lStorage: View = rootView.findViewById(R.id.l_storage)
    }
}