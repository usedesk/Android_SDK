package ru.usedesk.chat_gui.external;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.Set;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import ru.usedesk.chat_gui.R;
import ru.usedesk.chat_gui.internal.chat.AttachedFilesAdapter;
import ru.usedesk.chat_gui.internal.chat.ChatViewModel;
import ru.usedesk.chat_gui.internal.chat.ChatViewModelFactory;
import ru.usedesk.chat_gui.internal.chat.FilePicker;
import ru.usedesk.chat_gui.internal.chat.MessagePanelAdapter;
import ru.usedesk.chat_gui.internal.chat.MessagesAdapter;
import ru.usedesk.chat_gui.internal.chat.OfflineFormExpectedAdapter;
import ru.usedesk.chat_gui.internal.chat.OfflineFormSentAdapter;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage;
import ru.usedesk.common_gui.external.UsedeskViewCustomizer;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

@RuntimePermissions
public class UsedeskChatFragment extends Fragment {
    private MessagePanelAdapter messagePanelAdapter;
    private MessagesAdapter messagesAdapter;
    private OfflineFormExpectedAdapter offlineFormExpectedAdapter;
    private OfflineFormSentAdapter offlineFormSentAdapter;
    private AttachedFilesAdapter attachedFilesAdapter;

    private TextView tvLoading;
    private ViewGroup ltContent;

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

        viewModel = ViewModelProviders.of(this, new ChatViewModelFactory(container.getContext()))
                .get(ChatViewModel.class);

        initUi(view);
        renderData();
        observeData(getViewLifecycleOwner());

        return view;
    }

    private void initUi(@NonNull View view) {
        tvLoading = view.findViewById(R.id.tv_loading);
        ltContent = view.findViewById(R.id.lt_content);

        attachedFilesAdapter = new AttachedFilesAdapter(viewModel, view.findViewById(R.id.rv_attached_files));
        messagePanelAdapter = new MessagePanelAdapter(view, viewModel, v -> openAttachmentDialog(),
                getViewLifecycleOwner());
        offlineFormExpectedAdapter = new OfflineFormExpectedAdapter(view, viewModel, getViewLifecycleOwner());
        offlineFormSentAdapter = new OfflineFormSentAdapter(view, viewModel, getViewLifecycleOwner());
        messagesAdapter = new MessagesAdapter(view, viewModel,
                viewModel.getMessagesLiveData().getValue(),
                viewModel.getFeedbacksLiveData().getValue());
    }

    private void renderData() {
        onMessages(viewModel.getMessagesLiveData().getValue());
        onFileInfoList(viewModel.getFileInfoListLiveData().getValue());
    }

    private void observeData(@NonNull LifecycleOwner lifecycleOwner) {
        viewModel.getMessagesLiveData()
                .observe(lifecycleOwner, this::onMessages);

        viewModel.getFileInfoListLiveData()
                .observe(lifecycleOwner, this::onFileInfoList);

        viewModel.getFeedbacksLiveData()
                .observe(lifecycleOwner, this::onFeedbacks);

        viewModel.getExceptionLiveData()
                .observe(lifecycleOwner, this::onException);
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

    private void onException(UsedeskException exception) {
        if (exception != null) {
            String message = exception.getMessage();
            if (message == null) {
                message = exception.toString();
            }
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void onFeedbacks(@Nullable Set<Integer> feedbacks) {
        if (feedbacks != null) {
            messagesAdapter.updateFeedbacks(feedbacks);
        }
    }

    private void onMessages(@Nullable List<UsedeskMessage> usedeskMessages) {
        boolean isMessages = usedeskMessages != null;
        tvLoading.setVisibility(isMessages
                ? View.GONE
                : View.VISIBLE);
        ltContent.setVisibility(isMessages
                ? View.VISIBLE
                : View.GONE);

        if (usedeskMessages != null) {
            messagesAdapter.updateMessages(usedeskMessages);
        }
    }

    private void onFileInfoList(@Nullable List<UsedeskFileInfo> usedeskFileInfoList) {
        if (usedeskFileInfoList != null) {
            attachedFilesAdapter.update(usedeskFileInfoList);
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        UsedeskChatFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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