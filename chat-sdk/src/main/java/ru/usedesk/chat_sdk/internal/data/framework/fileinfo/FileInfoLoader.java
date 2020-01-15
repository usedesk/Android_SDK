package ru.usedesk.chat_sdk.internal.data.framework.fileinfo;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import ru.usedesk.chat_sdk.external.entity.UsedeskFile;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public class FileInfoLoader implements IFileInfoLoader {
    private static final String CONTENT_FORMAT = "data:%1s;base64,%2s";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int BUF_SIZE = 100 * 1024;

    private final Context context;

    @Inject
    FileInfoLoader(@NonNull Context context) {
        this.context = context;
    }

    private int transferTo(@NonNull InputStream inputStream, @NonNull OutputStream outputStream) throws IOException {
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
    private String convertToBase64(@NonNull Context context, @NonNull Uri uri) throws IOException {
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

    private String getMimeType(@NonNull Uri uri) {
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
    @Override
    public UsedeskFile getFrom(@NonNull UsedeskFileInfo fileInfo) throws UsedeskException {
        try {
            File file = new File(fileInfo.getUri().getPath());

            String base64 = convertToBase64(context, fileInfo.getUri());

            String mimeType = getMimeType(fileInfo.getUri());
            String content = String.format(CONTENT_FORMAT, mimeType, base64);
            String size = null;
            String name = file.getName();

            return new UsedeskFile(content, mimeType, size, name);
        } catch (IOException e) {
            throw new UsedeskDataNotFoundException(e.getMessage());
        }
    }
}
