package ru.usedesk.chat_sdk.internal.data.framework.file;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.InputStream;

import javax.inject.Inject;

import ru.usedesk.chat_sdk.internal.data.framework.file.entity.LoadedFile;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskLargeFileSizeException;
import ru.usedesk.common_sdk.internal.UsedeskFileUtil;

public class FileLoader implements IFileLoader {
    private static final int MAX_FILE_SIZE = 150 * 1024 * 1024;
    private final ContentResolver contentResolver;

    @Inject
    public FileLoader(@NonNull ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @Override
    public LoadedFile load(Uri uri) throws Exception {
        try (InputStream inputStream = contentResolver.openInputStream(uri)) {
            if (inputStream != null) {
                int size = inputStream.available();
                if (size > MAX_FILE_SIZE) {
                    throw new UsedeskLargeFileSizeException("File can not be more than " + MAX_FILE_SIZE + " bytes");
                }
                byte[] bytes = new byte[size];
                int last = inputStream.read(bytes);
                String name = UsedeskFileUtil.getFileName(contentResolver, uri);
                String type = UsedeskFileUtil.getMimeType(contentResolver, uri);
                return new LoadedFile(name, size, type, bytes);
            } else throw new UsedeskDataNotFoundException("File not found: " + uri);
        }
    }
}
