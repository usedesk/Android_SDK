package ru.usedesk.sdk.ui.chat;

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
import ru.usedesk.sdk.domain.entity.chat.OfflineForm;

public class OfflineFormDialog extends DialogFragment {

    private EditText companyIdEditText;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText messageEditText;

    private String companyId;
    private String email;

    private OnOfflineFormSetListener onOfflineFormSetListener;

    public OfflineFormDialog() {
    }

    public static OfflineFormDialog newInstance(String companyId, String email,
                                                OnOfflineFormSetListener onOfflineFormSetListener) {
        OfflineFormDialog dialogFragment = new OfflineFormDialog();
        dialogFragment.companyId = companyId;
        dialogFragment.email = email;
        dialogFragment.onOfflineFormSetListener = onOfflineFormSetListener;
        return dialogFragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.usedesk_view_offline_form, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle(R.string.offline_form_dialog_title);
        alertDialogBuilder.setView(view);

        companyIdEditText = view.findViewById(R.id.company_id_edit_text);
        emailEditText = view.findViewById(R.id.email_edit_text);
        nameEditText = view.findViewById(R.id.name_edit_text);
        messageEditText = view.findViewById(R.id.message_edit_text);

        companyIdEditText.setText(companyId);
        emailEditText.setText(email);

        alertDialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            boolean companyIdEntered = !TextUtils.isEmpty(companyIdEditText.getText());
            boolean emailEntered = !TextUtils.isEmpty(emailEditText.getText());

            if (companyIdEntered && emailEntered) {
                OfflineForm offlineForm = new OfflineForm();
                offlineForm.setCompanyId(companyId);
                offlineForm.setEmail(email);
                offlineForm.setName(nameEditText.getText().toString());
                offlineForm.setMessage(messageEditText.getText().toString());

                onOfflineFormSetListener.onnOfflineFormSet(offlineForm);
            }

            dismiss();
        });

        alertDialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dismiss());

        return alertDialogBuilder.create();
    }

    public interface OnOfflineFormSetListener {

        void onnOfflineFormSet(OfflineForm offlineForm);
    }
}