package ru.usedesk.chat_gui.external;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import ru.usedesk.chat_gui.R;
import ru.usedesk.chat_gui.internal.chat.AttachedFilesAdapter;
import ru.usedesk.chat_gui.internal.chat.ChatModel;
import ru.usedesk.chat_gui.internal.chat.ChatViewModel;
import ru.usedesk.chat_gui.internal.chat.ChatViewModelFactory;
import ru.usedesk.chat_gui.internal.chat.FilePicker;
import ru.usedesk.chat_gui.internal.chat.MessageAdapter;
import ru.usedesk.chat_gui.internal.chat.MessagesAdapter;
import ru.usedesk.chat_gui.internal.chat.OfflineFormAdapter;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.common_gui.external.UsedeskViewCustomizer;

@RuntimePermissions
public class UsedeskChatFragment extends Fragment {

    private static final int SWITCHER_LOADING_STATE = 1;
    private static final int SWITCHER_LOADED_STATE = 0;

    private ViewSwitcher contentViewSwitcher;

    private MessageAdapter messageAdapter;
    private OfflineFormAdapter offlineFormAdapter;
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
                .createView(inflater, R.layout.usedesk_fragment_chat, container, false, R.style.Usedesk_Theme_Chat);

        viewModel = ViewModelProviders.of(this, new ChatViewModelFactory(getContext()))
                .get(ChatViewModel.class);

        initUI(view);

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
            offlineFormAdapter.setMessage(messageAdapter.getMessage());
            messageAdapter.show(false);
            offlineFormAdapter.show(true);
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

        attachedFilesAdapter = new AttachedFilesAdapter(viewModel, view.findViewById(R.id.rv_attached_files));
        offlineFormAdapter = new OfflineFormAdapter(view, viewModel);
        messageAdapter = new MessageAdapter(view, viewModel, v -> openAttachmentDialog());
        messagesAdapter = new MessagesAdapter(view, viewModel.getModelLiveData().getValue().getMessages(), viewModel);
    }

    private void openAttachmentDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View bottomSheetView = UsedeskViewCustomizer.getInstance()
                .createView(getActivity().getLayoutInflater(),
                        R.layout.usedesk_dialog_attachment, null, false, R.style.Usedesk_Theme_Chat);

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

    private void onPickPhotoClicked() {
        UsedeskChatFragmentPermissionsDispatcher.pickPhotoWithPermissionCheck(this);
    }

    private void onTakePhotoClicked() {
        UsedeskChatFragmentPermissionsDispatcher.takePhotoWithPermissionCheck(this);
    }

    private void onPickDocumentClicked() {
        UsedeskChatFragmentPermissionsDispatcher.pickDocumentWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void pickPhoto() {
        filePicker.pickImage(this);
    }

    @NeedsPermission({Manifest.permission.CAMERA})
    void takePhoto() {
        filePicker.takePhoto(this);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void pickDocument() {
        filePicker.pickDocument(this);
    }
}