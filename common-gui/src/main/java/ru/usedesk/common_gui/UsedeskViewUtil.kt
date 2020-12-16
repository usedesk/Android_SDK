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

fun <BINDING> inflateItem(container: ViewGroup,
                          layoutId: Int,
                          createBinding: (View) -> BINDING): BINDING {
    return inflateItem(LayoutInflater.from(container.context),
            container,
            layoutId,
            createBinding)
}

fun <BINDING> inflateItem(inflater: LayoutInflater,
                          container: ViewGroup?,
                          layoutId: Int,
                          createBinding: (View) -> BINDING): BINDING {
    val customStyleId = UsedeskResourceManager.getResourceId(R.style.Usedesk)
    val localInflater = inflater.cloneInContext(ContextThemeWrapper(inflater.context, customStyleId))
    val view = localInflater.inflate(layoutId, container, false)
    return createBinding(view)
}