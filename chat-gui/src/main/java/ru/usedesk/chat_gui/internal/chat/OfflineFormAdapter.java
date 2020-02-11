package ru.usedesk.chat_gui.internal.chat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.usedesk.chat_gui.R;

public class OfflineFormAdapter {
    private final ViewGroup rootView;
    private final EditText emailEditText;
    private final EditText nameEditText;
    private final EditText messageEditText;
    private final TextView sendTextView;

    private final ChatViewModel viewModel;


    public OfflineFormAdapter(@NonNull View parentView, @NonNull ChatViewModel viewModel) {
        this.viewModel = viewModel;

        rootView = parentView.findViewById(R.id.offline_form_layout);
        nameEditText = parentView.findViewById(R.id.offline_form_name_edit_text);
        emailEditText = parentView.findViewById(R.id.offline_form_email_edit_text);
        messageEditText = parentView.findViewById(R.id.offline_form_message_edit_text);
        sendTextView = parentView.findViewById(R.id.usedesk_offline_form_send_image_view);

        sendTextView.setOnClickListener(v -> onSend());
    }

    private void onSend() {
        viewModel.onSend(nameEditText.getText().toString(),
                emailEditText.getText().toString(),
                messageEditText.getText().toString());
    }

    public void show(boolean show) {
        rootView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setMessage(@NonNull String message) {
        messageEditText.setText(message);
    }

    public void setName(@Nullable String clientName) {
        nameEditText.setText(clientName);
    }

    public void setEmail(@Nullable String email) {
        emailEditText.setText(email);
    }
}
