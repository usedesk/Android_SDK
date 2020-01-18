package ru.usedesk.chat_gui.internal.chat;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.chat_gui.R;
import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;

public class OfflineFormDialog extends DialogFragment {

    private static final String KEY_MESSAGE = "keyMessage";

    private EditText companyIdEditText;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText messageEditText;

    private IUsedeskChatSdk usedeskChat;

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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.usedesk_dialog_offline_form, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder.setTitle(R.string.offline_form_dialog_title);
        alertDialogBuilder.setView(view);

        companyIdEditText = view.findViewById(R.id.et_company_id);
        emailEditText = view.findViewById(R.id.et_email);
        nameEditText = view.findViewById(R.id.name_edit_text);
        messageEditText = view.findViewById(R.id.message_edit_text);

        usedeskChat = UsedeskChatSdk.getInstance();

        if (getArguments() != null) {
            messageEditText.setText(getArguments().getString(KEY_MESSAGE, ""));
        }

        alertDialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            boolean companyIdEntered = !TextUtils.isEmpty(companyIdEditText.getText());
            boolean emailEntered = !TextUtils.isEmpty(emailEditText.getText());

            if (companyIdEntered && emailEntered) {
                OfflineForm offlineForm = new OfflineForm();
                offlineForm.setName(nameEditText.getText().toString());
                offlineForm.setMessage(messageEditText.getText().toString());

                usedeskChat.sendRx(offlineForm)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();
            }

            dismiss();
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dismiss());

        return alertDialogBuilder.create();
    }
}