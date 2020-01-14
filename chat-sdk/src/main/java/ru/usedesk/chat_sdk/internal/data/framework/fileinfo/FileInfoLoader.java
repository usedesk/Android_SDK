package ru.usedesk.chat_sdk.internal.data.framework.fileinfo;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.usedesk.chat_sdk.external.entity.UsedeskFile;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public class FileInfoLoader implements IFileInfoLoader {

    private final Context context;

    @Inject
    public FileInfoLoader(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public UsedeskFile getFrom(@NonNull UsedeskFileInfo fileInfo) throws UsedeskException {
        String content = "123";
        String type = "img";
        String size = "0";
        String name = "gal";
        return new UsedeskFile(content, type, size, name);
    }
}
