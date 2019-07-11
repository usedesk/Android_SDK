package ru.usedesk.sdk.external.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.internal.utils.AttachmentUtils;

class FilePicker {

    private static final int REQUEST_CODE_PICK_FILE = 38141;

    private static final String MIME_TYPE_ALL_IMAGES = "image/*";
    private static final String MIME_TYPE_ALL_DOCS = "*/*";

    private void pickFile(@NonNull Activity activity, @NonNull String mimeType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType(mimeType);

        activity.startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    void pickImage(@NonNull Activity activity) {
        pickFile(activity, MIME_TYPE_ALL_IMAGES);
    }

    void pickDocument(@NonNull Activity activity) {
        pickFile(activity, MIME_TYPE_ALL_DOCS);
    }

    @Nullable
    List<UsedeskFile> onResult(@NonNull Context context, int requestCode, @NonNull Intent data) {
        if (requestCode == REQUEST_CODE_PICK_FILE) {
            return AttachmentUtils.getUsedeskFiles(context, data);
        }
        return null;
    }
}
