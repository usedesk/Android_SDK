package ru.usedesk.common_gui

import android.content.Context
import android.content.res.TypedArray
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getStringOrThrow


object UsedeskStyleManager {
    private val styleMap = hashMapOf<Int, Int>()

    @JvmStatic
    fun getStyle(defaultStyleId: Int): Int {
        return styleMap[defaultStyleId] ?: defaultStyleId
    }

    @JvmStatic
    fun replaceStyle(defaultStyleId: Int, customStyleId: Int) {
        styleMap[defaultStyleId] = customStyleId
    }

    @JvmStatic
    fun getString(context: Context,
                  defaultStyleId: Int,
                  attrId: Int): String {
        return getValue(context, defaultStyleId, attrId) { attrs, index ->
            attrs.getStringOrThrow(index)
        }
    }

    @JvmStatic
    fun getInt(context: Context,
               defaultStyleId: Int,
               attrId: Int): Int {
        return getValue(context, defaultStyleId, attrId) { attrs, index ->
            attrs.getIntOrThrow(index)
        }
    }

    @JvmStatic
    fun getColor(context: Context,
                 defaultStyleId: Int,
                 attrId: Int): Int {
        return getValue(context, defaultStyleId, attrId) { attrs, index ->
            attrs.getColorOrThrow(index)
        }
    }

    private fun <T> getValue(context: Context,
                             defaultStyleId: Int,
                             attrId: Int,
                             onValue: (TypedArray, Int) -> T): T {
        val attrs = context.obtainStyledAttributes(
                getStyle(defaultStyleId),
                intArrayOf(attrId)
        )
        val value = onValue(attrs, 0)
        attrs.recycle()
        return value
    }
}