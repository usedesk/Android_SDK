package ru.usedesk.chat_gui.screens.utils;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;

public class AttachmentUtils {
    @NonNull
    private static List<Uri> getUriList(@NonNull Intent data) {
        Uri uri = data.getData();//single file
        ClipData clipData = data.getClipData();//list of files
        if (clipData != null) {
            List<Uri> uriList = new ArrayList<>(clipData.getItemCount());
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                uri = item.getUri();
                if (uri != null) {
                    uriList.add(uri);
                }
            }
            return uriList;
        } else if (uri != null) {
            return new ArrayList<>(Collections.singletonList(uri));
        }
        return new ArrayList<>();
    }

    private static String getMimeType(@NonNull Context context, @NonNull Uri uri) {
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            return context.getApplicationContext()
                    .getContentResolver()
                    .getType(uri);
        } else {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                    .toLowerCase();
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
    }

    @NonNull
    public static List<UsedeskFileInfo> getUsedeskFileInfoList(@NonNull Context context, @NonNull Intent data) {
        return Stream.of(getUriList(data))
                .map(uri -> createUsedeskFileInfo(context, uri))
                .toList();
    }

    @NonNull
    public static List<UsedeskFileInfo> getUsedeskFileInfo(@NonNull Context context, @NonNull Uri uri) {
        return Collections.singletonList(createUsedeskFileInfo(context, uri));
    }

    @NonNull
    private static UsedeskFileInfo createUsedeskFileInfo(@NonNull Context context, @NonNull Uri uri) {
        String mimeType = getMimeType(context, uri);
        UsedeskFileInfo.Type type = UsedeskFileInfo.Type.getByMimeType(mimeType);
        return new UsedeskFileInfo(uri, type);
    }
}