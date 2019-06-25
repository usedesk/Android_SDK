package ru.usedesk.sdk.external.ui.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
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

import java.util.ArrayList;
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.fragments.BaseFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.UsedeskChat;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.Message;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.internal.AppSession;
import ru.usedesk.sdk.internal.utils.AttachmentUtils;
import ru.usedesk.sdk.internal.utils.NetworkUtils;

@RuntimePermissions
public class ChatFragment extends BaseFragment implements UsedeskActionListener {

    private static final int MAX_MESSAGE_LENGTH = 10000;
    private static final int MAX_FILES = 3;

    private static final int SWITCHER_LOADING_STATE = 1;
    private static final int SWITCHER_LOADED_STATE = 0;

    private ViewSwitcher contentViewSwitcher;
    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageButton sendImageButton;
    private TextView attachmentMarkerTextView;

    private List<Message> messages;
    private MessagesAdapter messagesAdapter;
    private UsedeskChat usedeskChat;

    private List<UsedeskFile> usedeskFiles;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.usedesk_fragment_chat, container, false);
        initUI(view);
        initList();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        UsedeskSdk.getUsedeskNotificationsServiceFactory()
                .stopService(getContext());

        usedeskChat = UsedeskSdk.initChat(getContext(),
                AppSession.getSession().getUsedeskConfiguration(), this);
    }

    @Override
    public void onStop() {
        super.onStop();

        UsedeskSdk.releaseChat();

        UsedeskSdk.getUsedeskNotificationsServiceFactory()
                .startService(getContext(), AppSession.getSession().getUsedeskConfiguration());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        usedeskFiles = null;

        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case FilePickerConst.REQUEST_CODE_PHOTO:
                    ArrayList<String> photoPaths = new ArrayList<>(
                            data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    if (!photoPaths.isEmpty()) {
                        usedeskFiles = AttachmentUtils.createUsedeskFiles(photoPaths);
                    }
                    break;
                case FilePickerConst.REQUEST_CODE_DOC:
                    ArrayList<String> documentPaths = new ArrayList<>(
                            data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    if (!documentPaths.isEmpty()) {
                        usedeskFiles = AttachmentUtils.createUsedeskFiles(documentPaths);
                    }
                    break;
            }

            if (usedeskFiles != null) {
                attachmentMarkerTextView.setVisibility(View.VISIBLE);
                sendImageButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ChatFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onConnected() {
        getActivity().runOnUiThread(() -> contentViewSwitcher.setDisplayedChild(SWITCHER_LOADED_STATE));
    }

    @Override
    public void onMessageReceived(Message message) {
        if (message != null) {
            notifyListItemInserted(message);
        }
    }

    @Override
    public void onMessagesReceived(List<Message> messages) {
        if (messages != null) {
            int startPosition = this.messages.isEmpty() ? 0 : this.messages.size() - 1;
            notifyListItemsInserted(messages, startPosition);
        }
    }

    @Override
    public void onServiceMessageReceived(Message message) {
        if (message != null) {
            notifyListItemInserted(message);
        }
    }

    @Override
    public void onOfflineFormExpected() {
        if (getFragmentManager().findFragmentByTag(OfflineFormDialog.class.getSimpleName()) == null) {
            OfflineFormDialog offlineFormDialog = OfflineFormDialog.newInstance(
                    usedeskChat.getUsedeskConfiguration().getCompanyId(),
                    usedeskChat.getUsedeskConfiguration().getEmail(),
                    messageEditText.getText().toString(),
                    offlineForm -> usedeskChat.sendOfflineForm(offlineForm));
            offlineFormDialog.show(getFragmentManager(), OfflineFormDialog.class.getSimpleName());
        }
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onError(final int errorResId) {
        getActivity().runOnUiThread(() ->
                Toast.makeText(getActivity(), errorResId, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onError(final Exception e) {
        getActivity().runOnUiThread(() ->
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show());
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
        sendImageButton.setOnClickListener(view12 -> attemptSend());

        attachmentMarkerTextView = view.findViewById(R.id.attachment_marker_text_view);
    }

    private void initList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        messages = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(getActivity(), messages, feedback -> sendFeedback(feedback));
        messagesRecyclerView.setAdapter(messagesAdapter);

        messagesRecyclerView.addOnLayoutChangeListener(
                (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if (bottom < oldBottom) {
                        messagesRecyclerView.postDelayed(() -> scrollToBottom(), 100);
                    }
                });
    }

    private void notifyListItemInserted(final Message message) {
        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            messages.add(message);
            messagesAdapter.notifyItemInserted(messages.size() - 1);
            scrollToBottom();
        });
    }

    private void notifyListItemsInserted(final List<Message> newMessages, final int startPosition) {
        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(() -> {
            messages.addAll(newMessages);
            messagesAdapter.notifyItemRangeChanged(startPosition, messages.size() - 1);
            scrollToBottom();
        });
    }

    private void attemptSend() {
        if (!NetworkUtils.isNetworkConnected(getActivity())) {
            showError(R.string.no_connections);
            return;
        }

        String textMessage = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(textMessage) && usedeskFiles == null) {
            messageEditText.requestFocus();
            return;
        }

        if (textMessage.length() > MAX_MESSAGE_LENGTH) {
            showError(R.string.long_message);
            return;
        }

        if (usedeskFiles != null) {
            usedeskChat.sendMessage(textMessage, usedeskFiles);
        } else {
            usedeskChat.sendTextMessage(textMessage);
        }

        messageEditText.setText("");
        usedeskFiles = null;
        attachmentMarkerTextView.setVisibility(View.GONE);
    }

    private void sendFeedback(Feedback feedback) {
        usedeskChat.sendFeedbackMessage(feedback);
    }

    private void openAttachmentDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        View bottomSheetView = getActivity().getLayoutInflater()
                .inflate(R.layout.usedesk_view_attachment_dialog, null);

        bottomSheetView.findViewById(R.id.pick_photo_button)
                .setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    onPickPhotoClicked();
                });

        bottomSheetView.findViewById(R.id.pick_document_button)
                .setOnClickListener(view -> {
                    bottomSheetDialog.dismiss();
                    onPickDocumentClicked();
                });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    public void onPickPhotoClicked() {
        ChatFragmentPermissionsDispatcher.pickPhotoWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void pickPhoto() {
        FilePickerBuilder.getInstance()
                .setMaxCount(MAX_FILES)
                .setSelectedFiles(new ArrayList<>())
                .pickPhoto(this);
    }

    public void onPickDocumentClicked() {
        ChatFragmentPermissionsDispatcher.pickDocumentWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void pickDocument() {
        FilePickerBuilder.getInstance()
                .setMaxCount(MAX_FILES)
                .setSelectedFiles(new ArrayList<>())
                .pickFile(this);
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            messagesRecyclerView.smoothScrollToPosition(messagesAdapter.getItemCount() - 1);
        }
    }

    private void showError(int messageResId) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.error)
                .setMessage(messageResId)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}