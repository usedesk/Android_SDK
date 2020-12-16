package ru.usedesk.chat_gui

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog

abstract class UsedeskBottomSheetDialog(
        context: Context,
        protected val defaultStyleId: Int
) : BottomSheetDialog(context, UsedeskStyleManager.getStyle(defaultStyleId)) {

}