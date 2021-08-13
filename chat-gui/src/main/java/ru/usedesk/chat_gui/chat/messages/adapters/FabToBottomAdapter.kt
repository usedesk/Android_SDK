package ru.usedesk.chat_gui.chat.messages.adapters

import androidx.lifecycle.LifecycleOwner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.common_gui.visibleGone

internal class FabToBottomAdapter(
    fabToBottom: FloatingActionButton,
    viewModel: MessagesViewModel,
    lifecycleOwner: LifecycleOwner
) {
    private var lastButton: Boolean? = null

    init {
        viewModel.fabToBottomLiveData.observe(lifecycleOwner) {
            if (it != null) {
                if (lastButton == null) {
                    fabToBottom.visibility = visibleGone(it)
                } else {
                    if (it == true && lastButton == false) {
                        //SHOW
                        fabToBottom.animate()
                            .setDuration()
                        lastButton.anim
                    } else if (it == false && lastButton == true) {
                        //HIDE
                    }
                }
            }
        }
    }
}