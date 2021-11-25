package ru.usedesk.common_gui

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

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

fun <T> observe(lifecycleOwner: LifecycleOwner,
                liveData: LiveData<T?>,
                lambda: (T?) -> (Unit)) {
    liveData.observe(lifecycleOwner, Observer(lambda))
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
    view.post {
        getInputMethodManager(view)?.hideSoftInputFromWindow(view.windowToken, 0)
    }
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

fun <BINDING> inflateItem(container: ViewGroup,
                          defaultLayoutId: Int,
                          defaultStyleId: Int,
                          createBinding: (View, Int) -> BINDING): BINDING {
    return inflateItem(LayoutInflater.from(container.context),
            container,
            defaultLayoutId,
            defaultStyleId,
            createBinding)
}

fun <BINDING> inflateItem(inflater: LayoutInflater,
                          container: ViewGroup?,
                          defaultLayoutId: Int,
                          defaultStyleId: Int,
                          createBinding: (View, Int) -> BINDING): BINDING {
    val customStyleId = UsedeskResourceManager.getResourceId(defaultStyleId)
    val localInflater = inflater.cloneInContext(ContextThemeWrapper(inflater.context, customStyleId))
    val layoutId = UsedeskResourceManager.getResourceId(defaultLayoutId)
    val view = localInflater.inflate(layoutId, container, false)
    return createBinding(view, defaultStyleId)
}