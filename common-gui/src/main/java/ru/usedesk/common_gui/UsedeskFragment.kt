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

    protected fun showSnackbarError(binding: UsedeskBinding,
                                    messageAttrId: Int) {
        binding.styleValues.apply {
            UsedeskSnackbar.create(
                    this@UsedeskFragment,
                    getColor(R.attr.usedesk_common_error_background_color),
                    getString(messageAttrId),
                    getColor(R.attr.usedesk_common_error_text_color)
            ).show()
        }
    }

    protected inline fun <reified T> getParentListener(): T? {
        return when {
            parentFragment is T -> {
                parentFragment as T
            }
            activity is T -> {
                activity as T
            }
            else -> {
                null
            }
        }
    }
}