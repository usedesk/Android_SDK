package ru.usedesk.chat_gui.chat

import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskPermissionUtil
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.inflateItem
import java.io.File

internal class UsedeskAttachmentDialog private constructor(
    screen: UsedeskChatScreen,
    dialogStyle: Int
) : BottomSheetDialog(screen.requireContext(), dialogStyle) {

    private val binding: Binding
    private val getContent = screen.registerForActivityResult(GetMultipleContents()) { uris ->
        attachedUris = uris
    }

    private val takePicture = screen.registerForActivityResult(TakePicture()) {
        if (!it) {
            attachedUris = listOf()
        }
    }
    private var attachedUris: List<Uri> = listOf()

    init {
        val container = screen.view as ViewGroup

        binding = inflateItem(
            layoutInflater,
            container,
            R.layout.usedesk_dialog_attachment,
            dialogStyle
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        setContentView(binding.rootView)

        binding.lGallery.setOnClickListener {
            dismiss()
            UsedeskPermissionUtil.needReadExternalPermission(binding, screen) {
                pickImage()
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
                pickDocument()
            }
        }

        BottomSheetBehavior.from(binding.rootView.parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun getAttachedUri(clearAfter: Boolean = false): List<Uri> {
        val uri = attachedUris
        if (clearAfter) {
            attachedUris = listOf()
        }
        return uri
    }

    private fun pickImage() {
        getContent.launch(MIME_TYPE_ALL_IMAGES)
    }

    private fun pickDocument() {
        getContent.launch(MIME_TYPE_ALL_DOCS)
    }

    private fun takePhoto(fragment: Fragment) {
        val applicationContext = fragment.requireContext().applicationContext
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
        attachedUris = listOf(photoUri)
        takePicture.launch(photoUri)
    }

    private fun getTakePhotoUri(context: Context): Uri {
        val fileName = "camera_${System.currentTimeMillis()}.jpg"
        return Uri.fromFile(File(context.externalCacheDir, fileName))
    }

    companion object {
        private const val MIME_TYPE_ALL_IMAGES = "*/*"
        private const val MIME_TYPE_ALL_DOCS = "*/*"

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