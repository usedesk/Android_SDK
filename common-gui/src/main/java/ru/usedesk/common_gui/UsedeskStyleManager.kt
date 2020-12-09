package ru.usedesk.common_gui

import android.content.Context
import android.content.res.TypedArray
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionOrThrow
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
    fun getStyleValues(context: Context, defaultStyleId: Int): StyleValues {
        return StyleValues(context, defaultStyleId)
    }

    class StyleValues(
            private val context: Context,
            private val defaultStyleId: Int
    ) {

        fun getString(attrId: Int): String {
            return getValue(attrId) { attrs, index ->
                attrs.getStringOrThrow(index)
            }
        }

        fun getInt(attrId: Int): Int {
            return getValue(attrId) { attrs, index ->
                attrs.getIntOrThrow(index)
            }
        }

        fun getColor(attrId: Int): Int {
            return getValue(attrId) { attrs, index ->
                attrs.getColorOrThrow(index)
            }
        }

        fun getPixels(attrId: Int): Float {
            return getValue(attrId) { attrs, index ->
                attrs.getDimensionOrThrow(index)
            }
        }

        private fun <T> getValue(attrId: Int,
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
}