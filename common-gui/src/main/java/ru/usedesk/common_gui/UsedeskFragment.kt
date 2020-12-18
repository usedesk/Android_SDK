package ru.usedesk.common_gui

import android.view.Gravity
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class UsedeskFragment : Fragment() {
    private var inited = false

    protected fun doInit(init: () -> Unit) {
        if (!inited) {
            inited = true
            init()
        }
    }

    open fun onBackPressed(): Boolean = false

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

    protected fun showSnackbarError(binding: UsedeskBinding,
                                    messageAttrId: Int) {
        val message = binding.styleValues.getString(messageAttrId)
        val backgroundColor = binding.styleValues.getColor(R.attr.usedesk_common_error_background_color)
        val textColor = binding.styleValues.getColor(R.attr.usedesk_common_error_text_color)
        Snackbar.make(binding.rootView, message, Snackbar.LENGTH_LONG).apply {
            view.setBackgroundColor(backgroundColor)
            view.findViewById<TextView>(R.id.snackbar_text).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
            setTextColor(textColor)
        }.show()
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