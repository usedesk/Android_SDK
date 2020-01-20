package ru.usedesk.chat_gui.internal.chat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import ru.usedesk.chat_gui.R;

public class MessageAdapter {

    private final ViewGroup rootView;
    private final ImageButton attachFileImageButton;
    private final EditText messageEditText;
    private final ImageButton sendImageButton;

    private final ChatViewModel viewModel;

    public MessageAdapter(@NonNull View parentView, @NonNull ChatViewModel viewModel, @NonNull View.OnClickListener onClickAttach) {
        this.viewModel = viewModel;

        rootView = parentView.findViewById(R.id.message_layout);

        attachFileImageButton = parentView.findViewById(R.id.attach_file_image_view);
        attachFileImageButton.setOnClickListener(onClickAttach);

        messageEditText = parentView.findViewById(R.id.message_edit_text);

        sendImageButton = parentView.findViewById(R.id.send_image_view);
        sendImageButton.setOnClickListener(v -> onSendClick());
    }

    private void onSendClick() {
        viewModel.onSend(messageEditText.getText().toString().trim());
        messageEditText.setText("");
    }

    public void show(boolean show) {
        rootView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
