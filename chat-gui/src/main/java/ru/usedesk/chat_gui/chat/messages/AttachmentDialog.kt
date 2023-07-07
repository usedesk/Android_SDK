package ru.usedesk.chat_gui.chat.messages

import android.view.View
import android.view.ViewGroup
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskBottomSheetDialog
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.inflateItem

internal class AttachmentDialog private constructor(
    private val screen: UsedeskFragment,
    dialogStyle: Int
) : UsedeskBottomSheetDialog(screen.requireContext(), dialogStyle) {

    init {
        inflateItem(
            layoutInflater,
            screen.view as ViewGroup,
            R.layout.usedesk_dialog_attachment,
            dialogStyle,
            ::Binding
        ).apply {
            setContentView(rootView)

            lGallery.setOnClickListener {
                dismiss()
                screen.startImages()
            }

            lCamera.setOnClickListener {
                dismiss()
                screen.needCameraPermission()
            }
            lStorage.setOnClickListener {
                dismiss()
                screen.startFiles()
            }
        }
    }

    companion object {
        fun create(screen: UsedeskFragment) = AttachmentDialog(
            screen,
            UsedeskResourceManager.getResourceId(R.style.Usedesk_Chat_Attachment_Dialog)
        )
    }

    private class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val lGallery: View = rootView.findViewById(R.id.l_gallery)
        val lCamera: View = rootView.findViewById(R.id.l_camera)
        val lStorage: View = rootView.findViewById(R.id.l_storage)
    }
}