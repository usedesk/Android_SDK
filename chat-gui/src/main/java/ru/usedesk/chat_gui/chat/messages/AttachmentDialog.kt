package ru.usedesk.chat_gui.chat.messages

import android.view.View
import android.view.ViewGroup
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskBottomSheetDialog
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.chat_gui.R as chatR

internal class AttachmentDialog private constructor(
    private val screen: UsedeskFragment,
    dialogStyle: Int
) : UsedeskBottomSheetDialog(screen.requireContext(), dialogStyle) {

    init {
        inflateItem(
            inflater = layoutInflater,
            container = screen.view as ViewGroup,
            defaultLayoutId = chatR.layout.usedesk_dialog_attachment,
            defaultStyleId = dialogStyle,
            createBinding = ::Binding,
        ).apply {
            setContentView(rootView)

            lGallery.setOnClickListener {
                dismiss()
                screen.startImages()
            }

            lCamera.setOnClickListener {
                dismiss()
                screen.startCamera()
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
            chatR.style.Usedesk_Chat_Attachment_Dialog
        )
    }

    private class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val lGallery: View = rootView.findViewById(chatR.id.l_gallery)
        val lCamera: View = rootView.findViewById(chatR.id.l_camera)
        val lStorage: View = rootView.findViewById(chatR.id.l_storage)
    }
}