package ru.usedesk.common_gui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.GsonBuilder

abstract class UsedeskFragment : Fragment() {

    open fun onBackPressed(): Boolean = false

    private val gson: Gson = GsonBuilder().create()

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

    protected fun <T> argsGetObject(key: String, clazz: Class<T>): T? {
        try {
            val json = argsGetString(key)
            if (json != null) {
                return gson.fromJson(json, clazz)
            }
        } catch (e: Exception) {

        }
        return null
    }

    protected fun <T> argsGetObject(key: String, clazz: Class<T>, default: T): T {
        return argsGetObject(key, clazz) ?: default
    }

    protected fun argsPutObject(args: Bundle, key: String, obj: Any) {
        args.putString(key, gson.toJson(obj))
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

    inline fun <reified T> getParentListener(): T? {
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