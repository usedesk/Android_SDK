package ru.usedesk.chat_gui.internal.chat;

import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import ru.usedesk.chat_gui.R;

public class OfflineFormExpectedAdapter {
    private final ViewGroup rootView;
    private final EditText emailEditText;
    private final EditText nameEditText;
    private final EditText messageEditText;
    private final TextView sendTextView;

    private final ChatViewModel viewModel;


    public OfflineFormExpectedAdapter(@NonNull View parentView, @NonNull ChatViewModel viewModel,
                                      @NonNull LifecycleOwner lifecycleOwner) {
        this.viewModel = viewModel;

        rootView = parentView.findViewById(R.id.offline_form_layout);
        nameEditText = parentView.findViewById(R.id.offline_form_name_edit_text);
        emailEditText = parentView.findViewById(R.id.offline_form_email_edit_text);
        messageEditText = parentView.findViewById(R.id.offline_form_message_edit_text);
        sendTextView = parentView.findViewById(R.id.usedesk_offline_form_send_image_view);

        sendTextView.setOnClickListener(v -> onSend());

        updateFields();

        onMessagePanelState(viewModel.getMessagePanelStateLiveData().getValue());
        viewModel.getMessagePanelStateLiveData().observe(lifecycleOwner, this::onMessagePanelState);
        viewModel.getMessageLiveData().observe(lifecycleOwner, message -> validateFields());
        viewModel.getNameLiveData().observe(lifecycleOwner, name -> validateFields());
        viewModel.getEmailLiveData().observe(lifecycleOwner, email -> validateFields());

        messageEditText.addTextChangedListener(new TextChangeListener(viewModel::onMessageChanged));
        nameEditText.addTextChangedListener(new TextChangeListener(viewModel::onNameChanged));
        emailEditText.addTextChangedListener(new TextChangeListener(viewModel::onEmailChanged));
    }

    private void validateFields() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String message = messageEditText.getText().toString();

        boolean nameCorrect = !name.isEmpty();
        boolean emailCorrect = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean messageCorrect = !message.isEmpty();

        sendTextView.setEnabled(nameCorrect && emailCorrect && messageCorrect);
    }

    private void onMessagePanelState(@Nullable MessagePanelState messagePanelState) {
        boolean offlineFormExpected = messagePanelState != null
                && messagePanelState.equals(MessagePanelState.OFFLINE_FORM_EXPECTED);

        rootView.setVisibility(offlineFormExpected
                ? View.VISIBLE
                : View.GONE);

        if (offlineFormExpected) {
            updateFields();
        }
    }

    private void updateFields() {
        messageEditText.setText(viewModel.getMessageLiveData().getValue());
        nameEditText.setText(viewModel.getNameLiveData().getValue());
        emailEditText.setText(viewModel.getEmailLiveData().getValue());
    }

    private void onSend() {
        viewModel.onSend(nameEditText.getText().toString(),
                emailEditText.getText().toString(),
                messageEditText.getText().toString());
    }
}
