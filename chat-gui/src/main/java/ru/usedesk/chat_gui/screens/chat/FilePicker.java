package ru.usedesk.chat_gui.screens.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.List;

import ru.usedesk.chat_gui.screens.utils.AttachmentUtils;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;

class FilePicker {

    private static final int REQUEST_CODE_PICK_FILE = 38141;
    private static final int REQUEST_CODE_TAKE_PHOTO = 38142;

    private static final String MIME_TYPE_ALL_IMAGES = "image/*";
    private static final String MIME_TYPE_ALL_DOCS = "*/*";

    private void pickFile(@NonNull Fragment fragment, @NonNull String mimeType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType(mimeType);

        fragment.startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    void pickImage(@NonNull Fragment fragment) {
        pickFile(fragment, MIME_TYPE_ALL_IMAGES);
    }

    void pickDocument(@NonNull Fragment fragment) {
        pickFile(fragment, MIME_TYPE_ALL_DOCS);
    }

    void takePhoto(@NonNull Fragment fragment) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTakePhotoUri(fragment.getContext()));
        fragment.startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO);
    }

    @Nullable
    List<UsedeskFileInfo> onResult(@NonNull Context context, int requestCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK_FILE: {
                if (data != null) {
                    return AttachmentUtils.getUsedeskFileInfoList(context, data);
                }
                break;
            }
            case REQUEST_CODE_TAKE_PHOTO: {
                return AttachmentUtils.getUsedeskFileInfo(context, getTakePhotoUri(context));
            }
        }
        return null;
    }

    @NonNull
    private Uri getTakePhotoUri(@NonNull Context context) {
        return Uri.fromFile(new File(context.getExternalCacheDir(), "camera.jpg"));
    }
}
