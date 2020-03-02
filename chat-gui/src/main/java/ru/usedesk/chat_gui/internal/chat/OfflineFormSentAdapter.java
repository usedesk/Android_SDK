package ru.usedesk.chat_gui.internal.chat;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import ru.usedesk.chat_gui.R;

public class OfflineFormSentAdapter {
    private final ViewGroup rootView;

    public OfflineFormSentAdapter(@NonNull View parentView, @NonNull ChatViewModel viewModel,
                                  @NonNull LifecycleOwner lifecycleOwner) {
        this.rootView = parentView.findViewById(R.id.offline_form_sent_layout);

        onMessagePanelState(viewModel.getMessagePanelStateLiveData().getValue());
        viewModel.getMessagePanelStateLiveData().observe(lifecycleOwner, this::onMessagePanelState);
    }

    private void onMessagePanelState(@Nullable MessagePanelState messagePanelState) {
        boolean messagePanel = messagePanelState != null && messagePanelState.isOfflineFormReceivedPanel();
        rootView.setVisibility(messagePanel
                ? View.VISIBLE
                : View.GONE);
    }
}
