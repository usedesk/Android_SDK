
package ru.usedesk.chat_gui.chat.offlineform

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.usedesk.chat_gui.R
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskBottomSheetDialog
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.inflateItem

internal class OfflineFormSuccessDialog private constructor(
    container: ViewGroup,
    dialogStyle: Int
) : UsedeskBottomSheetDialog(container.context, dialogStyle) {

    init {
        inflateItem(
            layoutInflater,
            container,
            R.layout.usedesk_dialog_offline_form_success,
            dialogStyle,
            ::Binding
        ).apply {
            setContentView(rootView)

            tvClose.setOnClickListener { dismiss() }
        }
    }

    companion object {
        fun newInstance(container: View) = OfflineFormSuccessDialog(
            container as ViewGroup,
            UsedeskResourceManager.getResourceId(R.style.Usedesk_Chat_Offline_Form_Success_Dialog)
        )
    }

    class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvClose: TextView = rootView.findViewById(R.id.tv_close)
    }
}