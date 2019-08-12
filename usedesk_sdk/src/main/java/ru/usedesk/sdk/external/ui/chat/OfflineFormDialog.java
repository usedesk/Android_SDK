package ru.usedesk.sdk.external.ui.chat;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.UsedeskChat;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.OfflineForm;

public class OfflineFormDialog extends DialogFragment {

    private static final String KEY_MESSAGE = "keyMessage";

    private EditText companyIdEditText;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText messageEditText;

    private String companyId;
    private String email;

    private UsedeskChat usedeskChat;

    public OfflineFormDialog() {
    }

    public static OfflineFormDialog newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);

        OfflineFormDialog dialogFragment = new OfflineFormDialog();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.usedesk_view_offline_form, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder.setTitle(R.string.offline_form_dialog_title);
        alertDialogBuilder.setView(view);

        companyIdEditText = view.findViewById(R.id.company_id_edit_text);
        emailEditText = view.findViewById(R.id.email_edit_text);
        nameEditText = view.findViewById(R.id.name_edit_text);
        messageEditText = view.findViewById(R.id.message_edit_text);

        usedeskChat = UsedeskSdk.getChat();

        this.companyId = usedeskChat.getUsedeskConfiguration().getCompanyId();
        this.email = usedeskChat.getUsedeskConfiguration().getEmail();

        companyIdEditText.setText(companyId);
        emailEditText.setText(email);
        if (getArguments() != null) {
            messageEditText.setText(getArguments().getString(KEY_MESSAGE, ""));
        }

        alertDialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            boolean companyIdEntered = !TextUtils.isEmpty(companyIdEditText.getText());
            boolean emailEntered = !TextUtils.isEmpty(emailEditText.getText());

            if (companyIdEntered && emailEntered) {
                OfflineForm offlineForm = new OfflineForm();
                offlineForm.setCompanyId(companyId);
                offlineForm.setEmail(email);
                offlineForm.setName(nameEditText.getText().toString());
                offlineForm.setMessage(messageEditText.getText().toString());

                usedeskChat.sendOfflineForm(offlineForm);
            }

            dismiss();
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dismiss());

        return alertDialogBuilder.create();
    }
}