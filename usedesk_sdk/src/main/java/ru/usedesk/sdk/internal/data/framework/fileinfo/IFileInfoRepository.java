package ru.usedesk.sdk.internal.data.framework.fileinfo;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.entity.chat.UsedeskFileInfo;
import ru.usedesk.sdk.external.entity.exceptions.UsedeskException;


public interface IFileInfoRepository {

    @NonNull
    UsedeskFile getFrom(@NonNull UsedeskFileInfo fileInfo) throws UsedeskException;
}
