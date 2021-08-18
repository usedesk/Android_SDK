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
    private var lastButton: Boolean? = null
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
        viewModel.fabToBottomLiveData.observe(lifecycleOwner) {
            if (it != null) {
                if (lastButton == null) {
                    fabToBottom.visibility = visibleGone(it)
                } else {
                    if (it == true && lastButton == false) {
                        fabToBottom.startAnimation(animationIn)
                    } else if (it == false && lastButton == true) {
                        fabToBottom.startAnimation(animationOut)
                    }
                }
                lastButton = it
            }
        }
    }
}