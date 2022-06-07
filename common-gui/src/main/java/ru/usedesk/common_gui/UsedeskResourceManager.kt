package ru.usedesk.common_gui

import android.content.Context
import android.content.res.TypedArray
import androidx.core.content.res.*

object UsedeskResourceManager {
    private val resourceMap = hashMapOf<Int, Int>()

    @JvmStatic
    fun getResourceId(defaultResourceId: Int): Int {
        return resourceMap[defaultResourceId] ?: defaultResourceId
    }

    @JvmStatic
    fun replaceResourceId(defaultResourceId: Int, customResourceId: Int) {
        resourceMap[defaultResourceId] = customResourceId
    }

    @JvmStatic
    fun getStyleValues(context: Context, defaultStyleId: Int): StyleValues {
        return StyleValues(context, defaultStyleId)
    }

    class StyleValues(
        private val context: Context,
        private val defaultStyleId: Int
    ) {

        fun findString(attrId: Int): String? {
            return getValue(attrId) { attrs, index ->
                attrs.getString(index)
            }
        }

        fun getString(attrId: Int): String {
            return getValue(attrId) { attrs, index ->
                attrs.getStringOrThrow(index)
            }
        }

        fun getStyleValues(attrId: Int): StyleValues {
            val styleId = getId(attrId)
            return StyleValues(context, styleId)
        }

        fun getInt(attrId: Int): Int {
            return getValue(attrId) { attrs, index ->
                attrs.getIntOrThrow(index)
            }
        }

        fun getId(attrId: Int): Int {
            return getValue(attrId) { attrs, index ->
                attrs.getResourceIdOrThrow(index)
            }
        }

        fun getIdOrZero(attrId: Int): Int {
            return getValue(attrId) { attrs, index ->
                attrs.getResourceId(index, 0)
            }
        }

        fun getStyle(attrId: Int): Int {
            return getValue(attrId) { attrs, index ->
                attrs.getResourceIdOrThrow(index)
            }
        }

        fun getColor(attrId: Int): Int {
            return getValue(attrId) { attrs, index ->
                attrs.getColorOrThrow(index)
            }
        }

        fun getFloat(attrId: Int): Float {
            return getValue(attrId) { attrs, index ->
                attrs.getDimensionOrThrow(index)
            }
        }

        fun getFloat(attrId: Int, default: Float): Float {
            return getValue(attrId) { attrs, index ->
                attrs.getDimension(index, default)
            }
        }

        private fun <T> getValue(
            attrId: Int,
            onValue: (TypedArray, Int) -> T
        ): T {
            val attrs = context.obtainStyledAttributes(
                getResourceId(defaultStyleId),
                intArrayOf(attrId)
            )
            val value = onValue(attrs, 0)
            attrs.recycle()
            return value
        }
    }
}