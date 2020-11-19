package ru.usedesk.common_gui.internal

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import ru.usedesk.common_gui.R
import java.util.*

fun <T> initAndObserve(lifecycleOwner: LifecycleOwner,
                       liveData: LiveData<T?>,
                       lambda: (T?) -> (Unit)) {
    justInit(liveData, lambda)
    observe(lifecycleOwner, liveData, lambda)
}

fun <T> justInit(liveData: LiveData<T?>,
                 lambda: (T?) -> (Unit)) {
    lambda(liveData.value)
}

fun <T> initFirst(lifecycleOwner: LifecycleOwner,
                  liveData: LiveData<T?>,
                  lambda: (T?) -> (Boolean)) {
    var inited = false
    initAndObserve(lifecycleOwner, liveData) {
        if (!inited) {
            inited = lambda(it)
        }
    }
}

fun <T> observe(lifecycleOwner: LifecycleOwner,
                liveData: LiveData<T?>,
                lambda: (T?) -> (Unit)) {
    liveData.observe(lifecycleOwner, Observer(lambda))
}

fun argsGetInt(arguments: Bundle?, key: String, default: Int): Int {
    return arguments?.getInt(key, default) ?: default
}

fun argsGetInt(arguments: Bundle?, key: String): Int? {
    return if (arguments?.containsKey(key) == true) {
        arguments.getInt(key)
    } else {
        null
    }
}

fun argsGetLong(arguments: Bundle?, key: String, default: Long): Long {
    return arguments?.getLong(key, default) ?: default
}

fun argsGetLong(arguments: Bundle?, key: String): Long? {
    return if (arguments?.containsKey(key) == true) {
        arguments.getLong(key)
    } else {
        null
    }
}

fun argsGetBoolean(arguments: Bundle?, key: String, default: Boolean): Boolean {
    return arguments?.getBoolean(key, default) ?: default
}

fun argsGetBoolean(arguments: Bundle?, key: String): Boolean? {
    return if (arguments?.containsKey(key) == true) {
        arguments.getBoolean(key)
    } else {
        null
    }
}

fun argsGetString(arguments: Bundle?, key: String, default: String): String {
    return arguments?.getString(key) ?: default
}

fun argsGetString(arguments: Bundle?, key: String): String? {
    return arguments?.getString(key)
}

private fun getInputMethodManager(view: View) =
        view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?

fun showKeyboard(editText: EditText) {
    editText.postDelayed({
        editText.requestFocus()
        getInputMethodManager(editText)?.showSoftInput(editText, 0)
    }, 100)
}

fun showKeyboardIfFocused(editText: EditText) {
    if (editText.isFocused) {
        showKeyboard(editText)
    }
}

fun hideKeyboard(view: View) {
    view.run {
        getInputMethodManager(view)?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun getString(binding: ViewDataBinding, stringId: Int): String {
    return getString(binding.root, stringId)
}

fun getString(view: View, stringId: Int): String {
    return view.resources.getString(stringId)
}

fun visibleGone(visible: Boolean): Int {
    return if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun visibleInvisible(visible: Boolean): Int {
    return if (visible) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

fun showInstead(viewVisible: View,
                viewGone: View,
                firstShow: Boolean = true,
                gone: Boolean = true) {
    viewVisible.visibility = if (gone) {
        visibleGone(firstShow)
    } else {
        visibleInvisible(firstShow)
    }
    viewGone.visibility = if (gone) {
        visibleGone(!firstShow)
    } else {
        visibleInvisible(!firstShow)
    }
}

fun formatSize(context: Context, size: Long?): String {
    var sz = size ?: 0L
    var rank = 0
    while (sz >= 1024 && rank < 3) {
        sz /= 1024
        rank++
    }
    return String.format(Locale.getDefault(), "%d %s", sz,
            context.resources.getStringArray(R.array.size_postfixes)[rank])
}

fun <T : ViewDataBinding> inflateItem(layoutId: Int,
                                      parent: ViewGroup): T {
    return inflateItem(LayoutInflater.from(parent.context),
            layoutId,
            parent)
}

fun <T : ViewDataBinding> inflateItem(inflater: LayoutInflater,
                                      layoutId: Int,
                                      container: ViewGroup?): T {
    return DataBindingUtil.inflate(inflater,
            layoutId,
            container,
            false)
}

fun inflateFragment(inflater: LayoutInflater,
                    container: ViewGroup?,
                    themeId: Int,
                    layoutId: Int): ViewGroup {
    return inflater.cloneInContext(ContextThemeWrapper(inflater.context, themeId))
            .inflate(layoutId, container, false) as ViewGroup
}