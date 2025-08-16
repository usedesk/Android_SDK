package ru.usedesk.chat_gui.chat.messages

import android.view.View
import android.view.ViewGroup
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskBottomSheetDialog
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem

internal class AttachmentDialog private constructor(
    private val screen: UsedeskFragment,
    dialogStyle: Int
) : UsedeskBottomSheetDialog(screen.requireContext(), dialogStyle) {

    init {
        inflateItem(
            inflater = layoutInflater,
            container = screen.view as ViewGroup,
            defaultLayoutId = R.layout.usedesk_dialog_attachment,
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
            R.style.Usedesk_Chat_Attachment_Dialog
        )
    }

    private class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val lGallery: View = rootView.findViewById(R.id.l_gallery)
        val lCamera: View = rootView.findViewById(R.id.l_camera)
        val lStorage: View = rootView.findViewById(R.id.l_storage)
    }
}