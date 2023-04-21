
package ru.usedesk.chat_gui.chat.messages

import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

    private val binding: Binding

    init {
        val container = screen.view as ViewGroup

        binding = inflateItem(
            layoutInflater,
            container,
            R.layout.usedesk_dialog_attachment,
            dialogStyle,
            ::Binding
        )

        setContentView(binding.rootView)

        binding.lGallery.setOnClickListener {
            dismiss()
            screen.startImages()
        }

        binding.lCamera.setOnClickListener {
            dismiss()
            screen.needCameraPermission()
        }
        binding.lStorage.setOnClickListener {
            dismiss()
            screen.startFiles()
        }

        BottomSheetBehavior.from(binding.rootView.parent as View).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    companion object {
        fun create(screen: UsedeskFragment) = AttachmentDialog(
            screen,
            UsedeskResourceManager.getResourceId(R.style.Usedesk_Chat_Attachment_Dialog)
        )
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val lGallery: View = rootView.findViewById(R.id.l_gallery)
        val lCamera: View = rootView.findViewById(R.id.l_camera)
        val lStorage: View = rootView.findViewById(R.id.l_storage)
    }
}