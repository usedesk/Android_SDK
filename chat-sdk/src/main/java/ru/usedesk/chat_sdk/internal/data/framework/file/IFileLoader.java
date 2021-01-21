package ru.usedesk.chat_sdk.internal.data.framework.file;

import android.net.Uri;

import ru.usedesk.chat_sdk.internal.data.framework.file.entity.LoadedFile;

public interface IFileLoader {
    LoadedFile load(Uri uri) throws Exception;
}
