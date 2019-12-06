package ru.usedesk.sdk.external.ui.chat;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.AppSession;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.internal.utils.NetworkUtils;

@RuntimePermissions
public class ChatFragment extends Fragment {

    private static final int MAX_MESSAGE_LENGTH = 10000;

    private static final int SWITCHER_LOADING_STATE = 1;
    private static final int SWITCHER_LOADED_STATE = 0;

    private ViewSwitcher contentViewSwitcher;
    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageButton sendImageButton;
    private TextView attachmentMarkerTextView;

    private MessagesAdapter messagesAdapter;

    private FilePicker filePicker;

    private ChatViewModel viewModel;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filePicker = new FilePicker();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = UsedeskSdk.getUsedeskViewCustomizer()
                .createView(inflater, R.layout.usedesk_fragment_chat, container, false);

        viewModel = ViewModelProviders.of(this, new ChatViewModel.Factory(getContext()))
                .get(ChatViewModel.class);

        initUI(view);
        initList();

        viewModel.getModelLiveData()
                .observe(this, this::renderModel);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        UsedeskSdk.getUsedeskNotificationsServiceFactory()
                .stopService(getContext());
    }

    @Override
    public void onStop() {
        super.onStop();

        UsedeskSdk.getUsedeskNotificationsServiceFactory()
                .startService(getContext(), AppSession.getSession().getUsedeskConfiguration());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            List<UsedeskFile> selectedUsedeskFiles = filePicker.onResult(getContext(), requestCode, data);
            if (selectedUsedeskFiles != null) {
                viewModel.setUsedeskFiles(selectedUsedeskFiles);
            }
        }
    }

    private void renderModel(@NonNull ChatModel model) {
        if (!model.isLoading()) {
            contentViewSwitcher.setDisplayedChild(SWITCHER_LOADED_STATE);
        }

        if (model.getMessagesCountDif() > 0) {
            messagesAdapter.updateMessages(model.getMessages(), model.getMessagesCountDif());
        }

        if (model.isOfflineFormExpected()) {
            if (getFragmentManager().findFragmentByTag(OfflineFormDialog.class.getSimpleName()) == null) {
                OfflineFormDialog.newInstance(messageEditText.getText().toString())
                        .show(getFragmentManager(), OfflineFormDialog.class.getSimpleName());
            }
        }

        if (model.getUsedeskFiles().size() > 0) {
            attachmentMarkerTextView.setVisibility(View.VISIBLE);
            sendImageButton.setEnabled(true);
        } else {
            attachmentMarkerTextView.setVisibility(View.GONE);
        }

        if (model.getUsedeskException() != null) {
            Toast.makeText(getActivity(), model.getUsedeskException().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initUI(View view) {
        contentViewSwitcher = view.findViewById(R.id.content_view_switcher);
        contentViewSwitcher.setDisplayedChild(SWITCHER_LOADING_STATE);

        messagesRecyclerView = view.findViewById(R.id.messages_recycler_view);

        ImageButton attachFileImageButton = view.findViewById(R.id.attach_file_image_view);
        attachFileImageButton.setOnClickListener(view1 -> openAttachmentDialog());

        messageEditText = view.findViewById(R.id.message_edit_text);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                sendImageButton.setEnabled(!TextUtils.isEmpty(editable));
            }
        });

        sendImageButton = view.findViewById(R.id.send_image_view);
        sendImageButton.setEnabled(false);
        sendImageButton.setOnClickListener(v -> attemptSend());

        attachmentMarkerTextView = view.findViewById(R.id.attachment_marker_text_view);
    }

    private void initList() {
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        messagesAdapter = new MessagesAdapter(messagesRecyclerView, viewModel.getModelLiveData().getValue().getMessages(), viewModel::sendFeedback);
        messagesRecyclerView.setAdapter(messagesAdapter);

        messagesRecyclerView.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if (bottom < oldBottom) {
                        messagesRecyclerView.postDelayed(messagesAdapter::scrollToBottom, 100);
                    }
                });
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    private ChatModel getLastModel() {
        return viewModel.getModelLiveData().getValue();
    }

    private void attemptSend() {
        if (!NetworkUtils.isNetworkConnected(getContext())) {
            showError(R.string.no_connections);
            return;
        }

        String textMessage = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(textMessage) && getLastModel().getUsedeskFiles().size() == 0) {
            messageEditText.requestFocus();
            return;
        }

        if (textMessage.length() > MAX_MESSAGE_LENGTH) {
            showError(R.string.long_message);
            return;
        }

        viewModel.sendMessage(textMessage, getLastModel().getUsedeskFiles());

        messageEditText.setText("");
        attachmentMarkerTextView.setVisibility(View.GONE);
    }

    private void openAttachmentDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View bottomSheetView = UsedeskSdk.getUsedeskViewCustomizer()
                .createView(getActivity().getLayoutInflater(),
                        R.layout.usedesk_dialog_attachment, null, false);

        bottomSheetView.findViewById(R.id.pick_photo_button)
                .setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    onPickPhotoClicked();
                });

        bottomSheetView.findViewById(R.id.take_photo_button)
                .setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    onTakePhotoClicked();
                });

        bottomSheetView.findViewById(R.id.pick_document_button)
                .setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    onPickDocumentClicked();
                });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ChatFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void onPickPhotoClicked() {
        ChatFragmentPermissionsDispatcher.pickPhotoWithPermissionCheck(this);
    }

    public void onTakePhotoClicked() {
        ChatFragmentPermissionsDispatcher.takePhotoWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void pickPhoto() {
        filePicker.pickImage(this);
    }

    @NeedsPermission({Manifest.permission.CAMERA})
    public void takePhoto() {
        filePicker.takePhoto(this);
    }

    public void onPickDocumentClicked() {
        ChatFragmentPermissionsDispatcher.pickDocumentWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void pickDocument() {
        filePicker.pickDocument(this);
    }

    private void showError(int messageResId) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.error)
                .setMessage(messageResId)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}