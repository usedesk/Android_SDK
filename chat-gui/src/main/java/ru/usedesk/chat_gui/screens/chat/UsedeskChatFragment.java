package ru.usedesk.chat_gui.screens.chat;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import ru.usedesk.chat_gui.R;
import ru.usedesk.chat_gui.screens.utils.NetworkUtils;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.common_gui.external.UsedeskViewCustomizer;

@RuntimePermissions
public class UsedeskChatFragment extends Fragment {

    private static final int SWITCHER_LOADING_STATE = 1;
    private static final int SWITCHER_LOADED_STATE = 0;

    private ViewSwitcher contentViewSwitcher;
    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private ImageButton sendImageButton;

    private MessagesAdapter messagesAdapter;
    private AttachedFilesAdapter attachedFilesAdapter;

    private FilePicker filePicker;

    private ChatViewModel viewModel;

    public static UsedeskChatFragment newInstance() {
        return new UsedeskChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        filePicker = new FilePicker();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = UsedeskViewCustomizer.getInstance()
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

        UsedeskChatSdk.stopService(getContext());
    }

    @Override
    public void onStop() {
        super.onStop();

        UsedeskChatSdk.startService(getContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            List<UsedeskFileInfo> attachedFileInfoList = filePicker.onResult(getContext(),
                    requestCode, data);
            if (attachedFileInfoList != null) {
                viewModel.setAttachedFileInfoList(attachedFileInfoList);
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

        attachedFilesAdapter.update(model.getUsedeskFileInfoList());

        if (model.getUsedeskException() != null) {
            String message = model.getUsedeskException().getMessage();
            if (message == null) {
                message = model.getUsedeskException().toString();
            }
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void initUI(View view) {
        contentViewSwitcher = view.findViewById(R.id.content_view_switcher);
        contentViewSwitcher.setDisplayedChild(SWITCHER_LOADING_STATE);

        messagesRecyclerView = view.findViewById(R.id.messages_recycler_view);

        ImageButton attachFileImageButton = view.findViewById(R.id.attach_file_image_view);
        attachFileImageButton.setOnClickListener(view1 -> openAttachmentDialog());

        messageEditText = view.findViewById(R.id.message_edit_text);

        sendImageButton = view.findViewById(R.id.send_image_view);
        sendImageButton.setOnClickListener(v -> onSendClick());

        attachedFilesAdapter = new AttachedFilesAdapter(viewModel, view.findViewById(R.id.rv_attached_files));
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

    private void onSendClick() {
        if (!NetworkUtils.isNetworkConnected(getContext())) {
            showError(R.string.no_connections);
            return;
        }

        viewModel.onSend(messageEditText.getText().toString().trim());
    }

    private void openAttachmentDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View bottomSheetView = UsedeskViewCustomizer.getInstance()
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
        UsedeskChatFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void onPickPhotoClicked() {
        UsedeskChatFragmentPermissionsDispatcher.pickPhotoWithPermissionCheck(this);
    }

    public void onTakePhotoClicked() {
        UsedeskChatFragmentPermissionsDispatcher.takePhotoWithPermissionCheck(this);
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
        UsedeskChatFragmentPermissionsDispatcher.pickDocumentWithPermissionCheck(this);
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