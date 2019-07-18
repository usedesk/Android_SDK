package ru.usedesk.sdk.internal.utils;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Base64OutputStream;

import com.annimon.stream.Stream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ru.usedesk.sdk.external.entity.chat.UsedeskFile;

public class AttachmentUtils {

    private static final String CONTENT_FORMAT = "data:%1s;base64,%2s";

    @NonNull
    private static String convertToBase64(@NonNull Context context, @NonNull Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            byte[] buffer = new byte[10240];//specify the size to allow
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output64.write(buffer, 0, bytesRead);
            }

            return output.toString();
        } catch (Exception e) {
            LogUtils.LOGE(AttachmentUtils.class.getSimpleName(), e);
        }

        return "";
    }

    @NonNull
    private static List<Uri> getUriList(@NonNull Intent data) {
        Uri uri = data.getData();//single file
        if (uri != null) {
            List<Uri> uriList = new ArrayList<>(1);
            uriList.add(uri);
            return uriList;
        } else {
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
            }
        }
        return new ArrayList<>();
    }

    private static String getMimeType(@NonNull Context context, @NonNull Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.getType(uri);
    }

    @NonNull
    private static UsedeskFile createUsedeskFile(@NonNull Context context, @NonNull Uri uri) {
        File file = new File(uri.getPath());
        String mimeType = getMimeType(context, uri);

        UsedeskFile usedeskFile = new UsedeskFile();
        usedeskFile.setName(file.getName());
        usedeskFile.setContent(String.format(CONTENT_FORMAT, mimeType, convertToBase64(context, uri)));
        usedeskFile.setType(mimeType);

        return usedeskFile;
    }

    @NonNull
    public static List<UsedeskFile> getUsedeskFiles(@NonNull Context context, @NonNull Intent data) {
        return Stream.of(getUriList(data))
                .map(uri -> AttachmentUtils.createUsedeskFile(context, uri))
                .toList();
    }
}