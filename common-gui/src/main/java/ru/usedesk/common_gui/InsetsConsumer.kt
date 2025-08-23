package ru.usedesk.common_gui

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import kotlin.math.max


class InsetsConsumer(
    statusBar: Int,
    navigationBar: Int,
    ime: Int,
) {
    private val statusBar = IntApplier(statusBar)
    private val navigationBar = IntApplier(navigationBar)
    private val ime = IntApplier(ime)

    fun consumeStatusBar(): Int = statusBar.use()

    fun consumeNavigationBar(): Int = navigationBar.use()

    fun consumeIme(): Int = ime.use()

    fun consumeBottom(): Int = max(consumeIme(), consumeNavigationBar())

    fun getStatusBar(): Int = statusBar.value

    fun getNavigationBar(): Int = navigationBar.value

    fun getIme(): Int = ime.value

    fun getBottom(): Int = max(getNavigationBar(), getIme())

    private class IntApplier(var value: Int) {
        fun use(): Int = value.apply { value = 0 }
    }
}

fun View.insetsAsPaddings(
    ignoreStatusBar: Boolean = false,
    ignoreNavigationBar: Boolean = false,
    ignoreIme: Boolean = false,
) {
    applyInsets(
        ignoreStatusBar = ignoreStatusBar,
        ignoreIme = ignoreIme,
        ignoreNavigationBar = ignoreNavigationBar,
        initialTop = paddingTop,
        initialBottom = paddingBottom,
        applyResult = { top, bottom ->
            updatePadding(top = top, bottom = bottom)
        }
    )
}

fun View.insetsAsMargins(
    ignoreStatusBar: Boolean = false,
    ignoreNavigationBar: Boolean = false,
    ignoreIme: Boolean = false,
) {
    applyInsets(
        ignoreStatusBar = ignoreStatusBar,
        ignoreIme = ignoreIme,
        ignoreNavigationBar = ignoreNavigationBar,
        initialTop = (layoutParams as? MarginLayoutParams)?.topMargin ?: 0,
        initialBottom = (layoutParams as? MarginLayoutParams)?.bottomMargin ?: 0,
        applyResult = { top, bottom ->
            updateLayoutParams<MarginLayoutParams> {
                topMargin = top
                bottomMargin = bottom
            }
        }
    )
}

private fun View.applyInsets(
    ignoreStatusBar: Boolean,
    ignoreIme: Boolean,
    ignoreNavigationBar: Boolean,
    initialTop: Int,
    initialBottom: Int,
    applyResult: (top: Int, bottom: Int) -> Unit
) {
    if (ignoreStatusBar && ignoreIme && ignoreNavigationBar) return

    consumeInsets {
        val topInset = when {
            ignoreStatusBar -> 0
            else -> consumeStatusBar()
        }
        val bottomInset = when {
            ignoreIme && ignoreNavigationBar -> 0
            ignoreIme -> consumeNavigationBar()
            ignoreNavigationBar -> consumeIme()
            else -> consumeBottom()
        }
        applyResult(initialTop + topInset, initialBottom + bottomInset)
    }
}

fun View.consumeInsets(onInsetsConsumer: InsetsConsumer.() -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, windowInsets ->
        val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

        val insetsConsumer = InsetsConsumer(
            statusBar = systemBarsInsets.top,
            navigationBar = systemBarsInsets.bottom,
            ime = imeInsets.bottom,
        )

        onInsetsConsumer(insetsConsumer)

        WindowInsetsCompat.Builder(windowInsets)
            .setInsets(
                WindowInsetsCompat.Type.systemBars(),
                Insets.of(
                    systemBarsInsets.left,
                    insetsConsumer.getStatusBar(),
                    systemBarsInsets.right,
                    insetsConsumer.getNavigationBar(),
                )
            )
            .setInsets(
                WindowInsetsCompat.Type.ime(),
                Insets.of(
                    0,
                    0,
                    0,
                    insetsConsumer.getIme(),
                )
            ).build()
    }
}