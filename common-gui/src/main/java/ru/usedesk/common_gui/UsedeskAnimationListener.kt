package ru.usedesk.common_gui

import android.view.animation.Animation


class UsedeskAnimationListener(
        private val onStart: (animation: Animation?) -> Unit = {},
        private val onEnd: (animation: Animation?) -> Unit = {},
        private val onRepeat: (animation: Animation?) -> Unit = {}
) : Animation.AnimationListener {
    override fun onAnimationStart(animation: Animation?) {
        onStart(animation)
    }

    override fun onAnimationEnd(animation: Animation?) {
        onEnd(animation)
    }

    override fun onAnimationRepeat(animation: Animation?) {
        onRepeat(animation)
    }
}