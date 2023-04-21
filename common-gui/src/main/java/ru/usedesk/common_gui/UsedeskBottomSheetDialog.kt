
package ru.usedesk.common_gui

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog

abstract class UsedeskBottomSheetDialog(
    context: Context,
    protected val defaultStyleId: Int
) : BottomSheetDialog(context, UsedeskResourceManager.getResourceId(defaultStyleId))