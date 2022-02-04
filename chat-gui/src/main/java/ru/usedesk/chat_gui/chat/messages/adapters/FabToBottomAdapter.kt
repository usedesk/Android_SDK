package ru.usedesk.chat_gui.chat.messages.adapters

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.visibleGone

internal class FabToBottomAdapter(
    fabToBottom: FloatingActionButton,
    parentStyleValues: UsedeskResourceManager.StyleValues,
    viewModel: MessagesViewModel,
    lifecycleOwner: LifecycleOwner,
    onClickListener: () -> Unit
) {
    private val animationIn: Animation
    private val animationOut: Animation

    init {
        val fabStyleValues =
            parentStyleValues.getStyleValues(R.attr.usedesk_chat_screen_floating_action_button)
        animationIn = AnimationUtils.loadAnimation(
            fabToBottom.context,
            fabStyleValues.getId(R.attr.usedesk_animation_in)
        ).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    fabToBottom.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
        }
        animationOut = AnimationUtils.loadAnimation(
            fabToBottom.context,
            fabStyleValues.getId(R.attr.usedesk_animation_out)
        ).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    fabToBottom.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {
                    fabToBottom.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
        }
        fabToBottom.visibility = View.GONE
        fabToBottom.setOnClickListener {
            onClickListener()
        }
        viewModel.modelLiveData.initAndObserveWithOld(lifecycleOwner) { old, new ->
            if (old?.fabToBottom != new.fabToBottom) {
                if (old?.fabToBottom == null) {
                    fabToBottom.visibility = visibleGone(new.fabToBottom)
                } else if (new.fabToBottom && !old.fabToBottom) {
                    fabToBottom.startAnimation(animationIn)
                } else if (!new.fabToBottom && old.fabToBottom) {
                    fabToBottom.startAnimation(animationOut)
                }
            }
        }
    }
}