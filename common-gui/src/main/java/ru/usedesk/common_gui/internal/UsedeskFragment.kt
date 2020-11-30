package ru.usedesk.common_gui.internal

import android.content.res.TypedArray
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getStringOrThrow
import androidx.fragment.app.Fragment
import ru.usedesk.common_gui.external.UsedeskStyleManager

abstract class UsedeskFragment(
        protected val defaultStyleId: Int
) : Fragment() {
    private var inited = false

    protected fun doInit(init: () -> Unit) {
        if (!inited) {
            inited = true
            init()
        }
    }

    open fun onBackPressed(): Boolean = false

    fun getStringFromStyle(attrId: Int): String {
        return getValueFromStyle(attrId) { attrs, index ->
            attrs.getStringOrThrow(index)
        }
    }

    fun getIntFromStyle(attrId: Int): Int {
        return getValueFromStyle(attrId) { attrs, index ->
            attrs.getIntOrThrow(index)
        }
    }

    fun getColorFromStyle(attrId: Int): Int {
        return getValueFromStyle(attrId) { attrs, index ->
            attrs.getColorOrThrow(index)
        }
    }

    private fun <T> getValueFromStyle(attrId: Int,
                                      onValue: (TypedArray, Int) -> T): T {
        val attrs = requireContext().obtainStyledAttributes(
                UsedeskStyleManager.getStyle(defaultStyleId),
                intArrayOf(attrId)
        )
        val value = onValue(attrs, 0)
        attrs.recycle()
        return value
    }


    fun argsGetInt(key: String, default: Int): Int {
        return arguments?.getInt(key, default) ?: default
    }

    fun argsGetInt(key: String): Int? {
        val args = arguments
        return if (args?.containsKey(key) == true) {
            args.getInt(key)
        } else {
            null
        }
    }

    fun argsGetLong(key: String, default: Long): Long {
        return arguments?.getLong(key, default) ?: default
    }

    fun argsGetLong(key: String): Long? {
        val args = arguments
        return if (args?.containsKey(key) == true) {
            args.getLong(key)
        } else {
            null
        }
    }

    fun argsGetBoolean(key: String, default: Boolean): Boolean {
        return arguments?.getBoolean(key, default) ?: default
    }

    fun argsGetBoolean(key: String): Boolean? {
        val args = arguments
        return if (args?.containsKey(key) == true) {
            args.getBoolean(key)
        } else {
            null
        }
    }

    fun argsGetString(key: String, default: String): String {
        return arguments?.getString(key) ?: default
    }

    fun argsGetString(key: String): String? {
        return arguments?.getString(key)
    }
}