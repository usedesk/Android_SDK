package ru.usedesk.sample.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import ru.usedesk.sample.AppSession;
import ru.usedesk.sample.R;
import ru.usedesk.sample.utils.NetworkUtils;
import ru.usedesk.sdk.ChatFeedbackListener;
import ru.usedesk.sdk.UsedeskActionListener;
import ru.usedesk.sdk.UsedeskSDK;
import ru.usedesk.sdk.models.Feedback;
import ru.usedesk.sdk.models.Message;
import ru.usedesk.sdk.models.OfflineForm;
import ru.usedesk.sdk.models.UsedeskFile;

import static ru.usedesk.sdk.utils.AttachmentUtils.createUsedeskFile;

@RuntimePermissions
public class ChatFragment extends BaseFragment implements UsedeskActionListener {

    private static final String TAG = ChatFragment.class.getSimpleName();

    private static final int MAX_MESSAGE_LENGTH = 10000;

    private static final int SWITCHER_LOADING_STATE = 1;
    private static final int SWITCHER_LOADED_STATE = 0;

    private ViewSwitcher contentViewSwitcher;
    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageButton sendImageButton;
    private TextView attachmentMarkerTextView;

    private List<Message> messages;
    private MessagesAdapter messagesAdapter;
    private UsedeskSDK usedeskSDK;

    private UsedeskFile usedeskFile;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usedeskSDK = new UsedeskSDK.Builder(getActivity())
                .usedeskConfiguration(AppSession.getSession().getUsedeskConfiguration())
                .usedeskActionListener(this)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        initUI(view);
        initList();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        usedeskSDK.destroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        usedeskFile = null;

        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case FilePickerConst.REQUEST_CODE_PHOTO:
                    ArrayList<String> photoPaths = new ArrayList<>(
                            data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                    if (!photoPaths.isEmpty()) {
                        usedeskFile = createUsedeskFile(photoPaths);
                    }
                    break;
                case FilePickerConst.REQUEST_CODE_DOC:
                    ArrayList<String> documentPaths = new ArrayList<>(
                            data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                    if (!documentPaths.isEmpty()) {
                        usedeskFile = createUsedeskFile(documentPaths);
                    }
                    break;
            }

            if (usedeskFile != null) {
                attachmentMarkerTextView.setVisibility(View.VISIBLE);
                sendImageButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ChatFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onConnected() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contentViewSwitcher.setDisplayedChild(SWITCHER_LOADED_STATE);
            }
        });
    }

    @Override
    public void onMessageReceived(Message message) {
        notifyListItemInserted(message);
    }

    @Override
    public void onMessagesReceived(List<Message> messages) {
        int startPosition = this.messages.isEmpty() ? 0 : this.messages.size() - 1;
        notifyListItemsInserted(messages, startPosition);
    }

    @Override
    public void onServiceMessageReceived(Message message) {
        notifyListItemInserted(message);
    }

    @Override
    public void onOfflineFormExpected() {
        if (getFragmentManager().findFragmentByTag(OfflineFormDialog.class.getSimpleName()) == null) {
            OfflineFormDialog offlineFormDialog = OfflineFormDialog.newInstance(
                    usedeskSDK.getUsedeskConfiguration().getCompanyId(),
                    usedeskSDK.getUsedeskConfiguration().getEmail(),
                    new OfflineFormDialog.OnOfflineFormSetListener() {
                        @Override
                        public void onnOfflineFormSet(OfflineForm offlineForm) {
                            usedeskSDK.sendOfflineForm(offlineForm);
                        }
                    });
            offlineFormDialog.show(getFragmentManager(), OfflineFormDialog.class.getSimpleName());
        }
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onError(final int errorResId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), errorResId, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onError(final Exception e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initUI(View view) {
        contentViewSwitcher = view.findViewById(R.id.content_view_switcher);
        contentViewSwitcher.setDisplayedChild(SWITCHER_LOADING_STATE);

        messagesRecyclerView = (RecyclerView) view.findViewById(R.id.messages_recycler_view);

        ImageButton attachFileImageButton = (ImageButton) view.findViewById(R.id.attach_file_image_view);
        attachFileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAttachmentDialog();
            }
        });

        messageEditText = (EditText) view.findViewById(R.id.message_edit_text);
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

        sendImageButton = (ImageButton) view.findViewById(R.id.send_image_view);
        sendImageButton.setEnabled(false);
        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSend();
            }
        });

        attachmentMarkerTextView = (TextView) view.findViewById(R.id.attachment_marker_text_view);
    }

    private void initList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        messages = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(getActivity(), messages, new ChatFeedbackListener() {

            @Override
            public void onFeedbackSet(Feedback feedback) {
                sendFeedback(feedback);
            }
        });
        messagesRecyclerView.setAdapter(messagesAdapter);

        messagesRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                    int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    messagesRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollToBottom();
                        }
                    }, 100);
                }
            }
        });
    }

    private void notifyListItemInserted(final Message message) {
        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.add(message);
                messagesAdapter.notifyItemInserted(messages.size() - 1);
                scrollToBottom();
            }
        });
    }

    private void notifyListItemsInserted(final List<Message> newMessages, final int startPosition) {
        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.addAll(newMessages);
                messagesAdapter.notifyItemRangeChanged(startPosition, messages.size() - 1);
                scrollToBottom();
            }
        });
    }

    private void attemptSend() {
        if (!NetworkUtils.isNetworkConnected(getActivity())) {
            showError(R.string.no_connections);
            return;
        }

        String textMessage = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(textMessage) && usedeskFile == null) {
            messageEditText.requestFocus();
            return;
        }

        if (textMessage.length() > MAX_MESSAGE_LENGTH) {
            showError(R.string.long_message);
            return;
        }

        if (usedeskFile != null) {
            usedeskSDK.sendMessage(textMessage, usedeskFile);
        } else {
            usedeskSDK.sendTextMessage(textMessage);
        }

        messageEditText.setText("");
        usedeskFile = null;
        attachmentMarkerTextView.setVisibility(View.GONE);
    }

    private void sendFeedback(Feedback feedback) {
        usedeskSDK.sendFeedbackMessage(feedback);
    }

    private void openAttachmentDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        View bottomSheetView = getActivity().getLayoutInflater().inflate(R.layout.view_attachment_dialog, null);

        bottomSheetView.findViewById(R.id.pick_photo_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                onPickPhotoClicked();
            }
        });

        bottomSheetView.findViewById(R.id.pick_document_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                onPickDocumentClicked();
            }
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
                .setMaxCount(1)
                .setSelectedFiles(new ArrayList<String>())
                .pickPhoto(this);
    }

    public void onPickDocumentClicked() {
        ChatFragmentPermissionsDispatcher.pickDocumentWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void pickDocument() {
        FilePickerBuilder.getInstance()
                .setMaxCount(1)
                .setSelectedFiles(new ArrayList<String>())
                .pickFile(this);
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            messagesRecyclerView.smoothScrollToPosition(messagesAdapter.getItemCount() - 1);
        }
    }

    private void showError(int messageResId) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle(R.string.error)
                .setMessage(messageResId)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}