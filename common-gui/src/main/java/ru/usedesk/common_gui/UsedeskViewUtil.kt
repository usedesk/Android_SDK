
package ru.usedesk.common_gui

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


private fun getInputMethodManager(view: View) =
    view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?

fun showKeyboard(editText: EditText) {
    editText.postDelayed(
        {
            editText.requestFocus()
            getInputMethodManager(editText)?.showSoftInput(editText, 0)
        },
        100
    )
}

fun hideKeyboard(view: View) {
    view.post {
        getInputMethodManager(view)?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun visibleGone(visible: Boolean) = when {
    visible -> View.VISIBLE
    else -> View.GONE
}

fun visibleInvisible(visible: Boolean) = when {
    visible -> View.VISIBLE
    else -> View.INVISIBLE
}

fun showInstead(
    viewVisible: View,
    viewGone: View,
    firstShow: Boolean = true,
    gone: Boolean = true
) {
    viewVisible.visibility = when {
        gone -> visibleGone(firstShow)
        else -> visibleInvisible(firstShow)
    }
    viewGone.visibility = when {
        gone -> visibleGone(!firstShow)
        else -> visibleInvisible(!firstShow)
    }
}