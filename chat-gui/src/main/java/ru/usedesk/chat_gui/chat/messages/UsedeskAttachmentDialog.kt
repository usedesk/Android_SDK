package ru.usedesk.chat_gui.chat.messages

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.inflateItem
import java.io.File

internal class UsedeskAttachmentDialog private constructor(
    screen: UsedeskFragment,
    private val viewModel: MessagesViewModel,
    dialogStyle: Int
) : BottomSheetDialog(screen.requireContext(), dialogStyle) {

    private val binding: Binding

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

        setOnDismissListener {
            viewModel.showAttachmentPanel(false)
        }

        BottomSheetBehavior.from(binding.rootView.parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        viewModel.modelLiveData.initAndObserveWithOld(screen.viewLifecycleOwner) { old, new ->
            if (old?.actionEvent != new.actionEvent) {
                new.actionEvent?.process { action ->
                    when (action) {
                        MessagesViewModel.Action.FROM_CAMERA_PERMISSION -> {
                            screen.needCameraPermission(screen) {
                                viewModel.fromCameraAvailable()
                            }
                        }
                        MessagesViewModel.Action.FROM_GALLERY_PERMISSION -> {
                            screen.needReadExternalPermission(screen) {
                                viewModel.fromGalleryAvailable()
                            }
                        }
                        MessagesViewModel.Action.FROM_STORAGE_PERMISSION -> {
                            screen.needReadExternalPermission(screen) {
                                viewModel.fromStorageAvailable()
                            }
                        }
                        MessagesViewModel.Action.FROM_CAMERA -> {
                            val fileName = viewModel.modelLiveData.value.cameraUri
                            val uri = Uri.fromFile(File(context.externalCacheDir, fileName))
                            screen.fromCamera(uri)
                        }
                        MessagesViewModel.Action.FROM_GALLERY -> {
                            screen.fromGallery()
                        }
                        MessagesViewModel.Action.FROM_STORAGE -> {
                            screen.fromStorage()
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun create(
            screen: UsedeskFragment,
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