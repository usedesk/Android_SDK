package ru.usedesk.chat_gui

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.usedesk.common_gui.UsedeskResourceManager

abstract class UsedeskBottomSheetDialog(
        context: Context,
        protected val defaultStyleId: Int
) : BottomSheetDialog(context, UsedeskResourceManager.getResourceId(defaultStyleId))