package ru.usedesk.chat_sdk.internal.data.framework.fileinfo;

import androidx.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public interface IFileInfoLoader {

    @NonNull
    UsedeskFile getFrom(@NonNull UsedeskFileInfo fileInfo) throws UsedeskException;
}
