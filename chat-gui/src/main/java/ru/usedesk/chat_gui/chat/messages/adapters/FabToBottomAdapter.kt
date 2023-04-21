
package ru.usedesk.chat_gui.chat.messages.adapters

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.common_gui.onEachWithOld
import ru.usedesk.common_gui.visibleInvisible

internal class FabToBottomAdapter(
    fabContainer: ViewGroup,
    fabToBottom: FloatingActionButton,
    tvToBottom: TextView,
    parentStyleValues: UsedeskResourceManager.StyleValues,
    viewModel: MessagesViewModel,
    lifecycleCoroutineScope: LifecycleCoroutineScope,
    onClickListener: () -> Unit
) {
    private val animationIn: Animation
    private val animationOut: Animation

    init {
        val fabStyleValues = parentStyleValues.getStyleValues(
            R.attr.usedesk_chat_screen_floating_action_button
        )
        animationIn = AnimationUtils.loadAnimation(
            fabToBottom.context,
            fabStyleValues.getId(R.attr.usedesk_animation_in)
        ).apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    fabContainer.visibility = View.VISIBLE
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
                    fabContainer.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation?) {
                    fabContainer.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
        }
        fabContainer.visibility = View.INVISIBLE
        fabToBottom.setOnClickListener { onClickListener() }
        viewModel.modelFlow.onEachWithOld(lifecycleCoroutineScope) { old, new ->
            if (old?.fabToBottom != new.fabToBottom) {
                when {
                    old?.fabToBottom == null ->
                        fabContainer.visibility = visibleInvisible(new.fabToBottom)
                    new.fabToBottom && !old.fabToBottom -> fabContainer.startAnimation(animationIn)
                    !new.fabToBottom && old.fabToBottom -> fabContainer.startAnimation(animationOut)
                }
            }
            if (old?.agentMessageShowed != new.agentMessageShowed) {
                tvToBottom.run {
                    text = when {
                        new.agentMessageShowed > 99 -> "99+"
                        else -> new.agentMessageShowed.toString()
                    }
                    visibility = visibleInvisible(new.agentMessageShowed > 0)
                }
            }
            if (old?.goToBottom != new.goToBottom) {
                new.goToBottom?.use { onClickListener() }
            }
        }
    }
}