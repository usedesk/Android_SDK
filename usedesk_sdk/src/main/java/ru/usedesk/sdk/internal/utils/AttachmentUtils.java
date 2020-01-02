package ru.usedesk.sdk.internal.utils;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.webkit.MimeTypeMap;

import com.annimon.stream.Stream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.entity.chat.UsedeskFileInfo;

public class AttachmentUtils {

    private static final String CONTENT_FORMAT = "data:%1s;base64,%2s";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int BUF_SIZE = 100 * 1024;

    private static int transferTo(@NonNull InputStream inputStream, @NonNull OutputStream outputStream) throws IOException {
        int bytesRead;
        int offset = 0;
        byte[] buffer = new byte[BUF_SIZE];
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytesRead);
            offset += bytesRead;
        }
        return offset;
    }

    @NonNull
    private static String convertToBase64(@NonNull Context context, @NonNull Uri uri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new RuntimeException("Can't open file: " + uri.toString());
            }
            int size = inputStream.available();
            if (size > MAX_FILE_SIZE) {
                throw new RuntimeException("File size is bigger then " + MAX_FILE_SIZE);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);

            transferTo(inputStream, output64);

            return output.toString();
        }
    }

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

    @Nullable
    private static UsedeskFile createUsedeskFile(@NonNull Context context, @NonNull Uri uri) {
        try {
            File file = new File(uri.getPath());
            String mimeType = getMimeType(context, uri);

            UsedeskFile usedeskFile = new UsedeskFile();
            usedeskFile.setName(file.getName());
            usedeskFile.setContent(String.format(CONTENT_FORMAT, mimeType, convertToBase64(context, uri)));
            usedeskFile.setType(mimeType);

            return usedeskFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    public static List<UsedeskFileInfo> getUsedeskFiles(@NonNull Context context, @NonNull Intent data) {
        return Stream.of(getUriList(data))
                .map(UsedeskFileInfo::new)
                .toList();
    }

    /*@NonNull
    public static List<UsedeskFile> getUsedeskFileInfoList(@NonNull Context context, @NonNull Intent data) {
        return Stream.of(getUriList(data))
                .map(uri -> AttachmentUtils.createUsedeskFile(context, uri))
                .filter(file -> file != null)
                .toList();
    }*/

    @NonNull
    public static List<UsedeskFileInfo> getUsedeskFile(@NonNull Uri uri) {
        return Collections.singletonList(new UsedeskFileInfo(uri));
    }

    /*@NonNull
    public static List<UsedeskFile> getUsedeskFile(@NonNull Context context, @NonNull Uri uri) {
        UsedeskFile usedeskFile = AttachmentUtils.createUsedeskFile(context, uri);

        ArrayList<UsedeskFile> usedeskFiles = new ArrayList<>(1);
        if (usedeskFile != null) {
            usedeskFiles.add(usedeskFile);
        }
        return usedeskFiles;
    }*/
}