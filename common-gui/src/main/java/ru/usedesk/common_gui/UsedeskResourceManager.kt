
package ru.usedesk.common_gui

import android.content.Context
import android.content.res.TypedArray
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.getStringOrThrow

object UsedeskResourceManager {
    private val resourceMap = hashMapOf<Int, Int>()

    @JvmStatic
    fun getResourceId(defaultResourceId: Int): Int =
        resourceMap[defaultResourceId] ?: defaultResourceId

    @JvmStatic
    fun replaceResourceId(defaultResourceId: Int, customResourceId: Int) {
        resourceMap[defaultResourceId] = customResourceId
    }

    @JvmStatic
    fun getStyleValues(context: Context, defaultStyleId: Int): StyleValues =
        StyleValues(context, defaultStyleId)

    class StyleValues(
        private val context: Context,
        private val defaultStyleId: Int
    ) {

        fun findString(attrId: Int): String? = getValue(attrId, TypedArray::getString)

        fun getString(attrId: Int): String = getValue(attrId, TypedArray::getStringOrThrow)

        fun getStyleValues(attrId: Int): StyleValues = StyleValues(context, getId(attrId))

        fun getInt(attrId: Int): Int = getValue(attrId, TypedArray::getIntOrThrow)

        fun getId(attrId: Int): Int = getValue(attrId, TypedArray::getResourceIdOrThrow)

        fun getIdOrZero(attrId: Int): Int = getValue(attrId) { attrs, index ->
            attrs.getResourceId(index, 0)
        }

        fun getStyle(attrId: Int): Int = getValue(attrId, TypedArray::getResourceIdOrThrow)

        fun getColor(attrId: Int): Int = getValue(attrId, TypedArray::getColorOrThrow)

        fun getFloat(attrId: Int): Float = getValue(attrId, TypedArray::getDimensionOrThrow)

        fun getFloat(attrId: Int, default: Float): Float = getValue(attrId) { attrs, index ->
            attrs.getDimension(index, default)
        }

        private fun <T> getValue(
            attrId: Int,
            onValue: (TypedArray, Int) -> T
        ): T {
            val attrs = context.obtainStyledAttributes(
                getResourceId(defaultStyleId),
                intArrayOf(attrId)
            )
            return onValue(attrs, 0).apply {
                attrs.recycle()
            }
        }
    }
}