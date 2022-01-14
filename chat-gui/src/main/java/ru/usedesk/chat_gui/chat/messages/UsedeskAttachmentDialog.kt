package ru.usedesk.chat_gui.chat.messages

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
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskPermissionUtil
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.inflateItem
import java.io.File

internal class UsedeskAttachmentDialog private constructor(
    screen: Fragment,
    private val viewModel: MessagesViewModel,
    dialogStyle: Int
) : BottomSheetDialog(screen.requireContext(), dialogStyle) {

    private val binding: Binding

    private val getContent = screen.registerForActivityResult(GetMultipleContents()) { uriList ->
        val files = uriList.map { uri ->
            UsedeskFileInfo.create(
                context,
                uri
            )
        }
        viewModel.actionCompleted(files)
    }

    private val fromCamera = screen.registerForActivityResult(TakePicture()) {
        val fileName = viewModel.modelLiveData.value.cameraUri
        if (it && fileName != null) {
            val uri = Uri.fromFile(File(context.externalCacheDir, fileName))
            val file = UsedeskFileInfo.create(
                context,
                uri
            )
            viewModel.actionCompleted(listOf(file))
        } else {
            viewModel.resetAction()
        }
    }

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
            viewModel.fromGallery()
        }

        binding.lCamera.setOnClickListener {
            viewModel.fromCamera()
        }
        binding.lStorage.setOnClickListener {
            viewModel.fromStorage()
        }

        BottomSheetBehavior.from(binding.rootView.parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        viewModel.modelLiveData.initAndObserveWithOld(screen.viewLifecycleOwner) { old, new ->
            if (old?.actionEvent != new.actionEvent) {
                new.actionEvent?.process { action ->
                    when (action) {
                        MessagesViewModel.Action.FROM_CAMERA_PERMISSION -> {
                            UsedeskPermissionUtil.needCameraPermission(screen) {
                                viewModel.fromCameraAvailable()
                            }
                        }
                        MessagesViewModel.Action.FROM_GALLERY_PERMISSION -> {
                            UsedeskPermissionUtil.needReadExternalPermission(screen) {
                                viewModel.fromGalleryAvailable()
                            }
                        }
                        MessagesViewModel.Action.FROM_STORAGE_PERMISSION -> {
                            UsedeskPermissionUtil.needReadExternalPermission(screen) {
                                viewModel.fromStorageAvailable()
                            }
                        }
                        MessagesViewModel.Action.FROM_CAMERA -> {
                            fromCamera()
                        }
                        MessagesViewModel.Action.FROM_GALLERY -> {
                            fromGallery()
                        }
                        MessagesViewModel.Action.FROM_STORAGE -> {
                            fromStorage()
                        }
                    }
                }
            }
        }
    }

    fun release() {
        fromCamera.unregister()
        getContent.unregister()
    }

    private fun fromGallery() {
        getContent.launch(MIME_TYPE_ALL_IMAGES)
    }

    private fun fromStorage() {
        getContent.launch(MIME_TYPE_ALL_DOCS)
    }

    private fun fromCamera() {
        val applicationContext = context.applicationContext
        val fileName = viewModel.modelLiveData.value.cameraUri
        val uri = Uri.fromFile(File(context.externalCacheDir, fileName))
        val photoUri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri
        } else {
            FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.provider",
                File(uri.path ?: "")
            )
        }
        fromCamera.launch(photoUri)
    }

    companion object {
        private const val MIME_TYPE_ALL_IMAGES = "*/*"
        private const val MIME_TYPE_ALL_DOCS = "*/*"

        fun create(
            screen: Fragment,
            viewModel: MessagesViewModel
        ): UsedeskAttachmentDialog {
            val dialogStyle = UsedeskResourceManager.getResourceId(
                R.style.Usedesk_Chat_Attachment_Dialog
            )
            return UsedeskAttachmentDialog(screen, viewModel, dialogStyle)
        }
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val lGallery: View = rootView.findViewById(R.id.l_gallery)
        val lCamera: View = rootView.findViewById(R.id.l_camera)
        val lStorage: View = rootView.findViewById(R.id.l_storage)
    }
}