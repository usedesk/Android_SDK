package ru.usedesk.common_gui

import androidx.fragment.app.Fragment

abstract class UsedeskFragment : Fragment() {

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

    protected fun argsGetStringArray(key: String): Array<String>? {
        return arguments?.getStringArray(key)
    }

    protected fun argsGetStringArray(key: String, default: Array<String>): Array<String> {
        return arguments?.getStringArray(key) ?: default
    }

    protected fun showSnackbarError(styleValues: UsedeskResourceManager.StyleValues) {
        UsedeskSnackbar.create(
                this@UsedeskFragment,
                styleValues.getColor(R.attr.usedesk_background_color_1),
                styleValues.getString(R.attr.usedesk_text_1),
                styleValues.getColor(R.attr.usedesk_text_color_1)
        ).show()
    }

    protected inline fun <reified T> getParentListener(): T? {
        var listener: T? = null

        var parent = parentFragment
        while (parent != null) {
            if (parent is T) {
                listener = parent
                break
            } else {
                parent = parent.parentFragment
            }
        }

        if (listener == null) {
            if (activity is T) {
                listener = activity as T
            }
        }

        return listener
    }
}