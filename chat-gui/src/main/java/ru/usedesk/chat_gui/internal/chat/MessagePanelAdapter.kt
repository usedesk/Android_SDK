package ru.usedesk.chat_gui.internal.chat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import ru.usedesk.chat_gui.R;

public class MessagePanelAdapter {

    private final ViewGroup rootView;
    private final EditText messageEditText;
    private final ImageButton attachFileImageButton;
    private final ImageButton sendImageButton;

    private final ChatViewModel viewModel;

    public MessagePanelAdapter(@NonNull View parentView, @NonNull ChatViewModel viewModel,
                               @NonNull View.OnClickListener onClickAttach,
                               @NonNull LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;

        rootView = parentView.findViewById(R.id.message_layout);

        attachFileImageButton = parentView.findViewById(R.id.attach_file_image_view);
        attachFileImageButton.setOnClickListener(onClickAttach);

        messageEditText = parentView.findViewById(R.id.message_edit_text);

        sendImageButton = parentView.findViewById(R.id.send_image_view);
        sendImageButton.setOnClickListener(v -> onSendClick());

        onMessagePanelState(viewModel.getMessagePanelStateLiveData().getValue());
        viewModel.getMessagePanelStateLiveData().observe(lifecycleOwner, this::onMessagePanelState);

        messageEditText.setText(viewModel.getMessageLiveData().getValue());
        messageEditText.addTextChangedListener(new TextChangeListener(viewModel::onMessageChanged));
    }

    private void onMessagePanelState(@Nullable MessagePanelState messagePanelState) {
        boolean messagePanel = messagePanelState != null
                && messagePanelState.equals(MessagePanelState.MESSAGE_PANEL);

        rootView.setVisibility(messagePanel
                ? View.VISIBLE
                : View.GONE);

        if (messagePanel) {
            messageEditText.setText(viewModel.getMessageLiveData().getValue());
        }
    }

    private void onSendClick() {
        viewModel.onSend(messageEditText.getText().toString().trim());
        messageEditText.setText("");
    }
}
