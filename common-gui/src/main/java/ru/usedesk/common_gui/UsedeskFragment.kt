package ru.usedesk.common_gui

import android.content.res.TypedArray
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getStringOrThrow
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

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

    protected fun getStringFromStyle(attrId: Int): String {
        return getValueFromStyle(attrId) { attrs, index ->
            attrs.getStringOrThrow(index)
        }
    }

    protected fun getIntFromStyle(attrId: Int): Int {
        return getValueFromStyle(attrId) { attrs, index ->
            attrs.getIntOrThrow(index)
        }
    }

    protected fun getColorFromStyle(attrId: Int): Int {
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


    protected fun argsGetInt(key: String, default: Int): Int {
        return arguments?.getInt(key, default) ?: default
    }

    protected fun argsGetInt(key: String): Int? {
        val args = arguments
        return if (args?.containsKey(key) == true) {
            args.getInt(key)
        } else {
            null
        }
    }

    protected fun argsGetLong(key: String, default: Long): Long {
        return arguments?.getLong(key, default) ?: default
    }

    protected fun argsGetLong(key: String): Long? {
        val args = arguments
        return if (args?.containsKey(key) == true) {
            args.getLong(key)
        } else {
            null
        }
    }

    protected fun argsGetBoolean(key: String, default: Boolean): Boolean {
        return arguments?.getBoolean(key, default) ?: default
    }

    protected fun argsGetBoolean(key: String): Boolean? {
        val args = arguments
        return if (args?.containsKey(key) == true) {
            args.getBoolean(key)
        } else {
            null
        }
    }

    protected fun argsGetString(key: String, default: String): String {
        return arguments?.getString(key) ?: default
    }

    protected fun argsGetString(key: String): String? {
        return arguments?.getString(key)
    }

    protected fun showSnackbarError(stringId: Int) {
        view?.also {
            Snackbar.make(it, stringId, Snackbar.LENGTH_LONG).apply {
                view.setBackgroundColor(resources.getColor(R.color.usedesk_red))
                view.findViewById<TextView>(R.id.snackbar_text).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                setTextColor(resources.getColor(R.color.usedesk_white))
            }.show()
        }
    }

    protected inline fun <reified T> getParentListener(): T? {
        return when {
            activity is T -> {
                activity as T
            }
            parentFragment is T -> {
                parentFragment as T
            }
            else -> {
                null
            }
        }
    }
}