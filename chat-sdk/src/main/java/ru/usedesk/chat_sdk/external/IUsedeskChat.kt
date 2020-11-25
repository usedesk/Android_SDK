package ru.usedesk.chat_sdk.external;

import androidx.annotation.NonNull;

import java.util.List;

import io.reactivex.Completable;
import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessageButton;
import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public interface IUsedeskChat {
    void connect() throws UsedeskException;

    void disconnect() throws UsedeskException;

    void send(String textMessage) throws UsedeskException;

    void send(UsedeskFileInfo usedeskFileInfo) throws UsedeskException;

    void send(List<UsedeskFileInfo> usedeskFileInfoList) throws UsedeskException;

    void send(UsedeskFeedback feedback) throws UsedeskException;

    void send(UsedeskOfflineForm offlineForm) throws UsedeskException;

    void send(UsedeskMessageButton messageButton) throws UsedeskException;

    @NonNull
    Completable connectRx();

    @NonNull
    Completable disconnectRx();

    @NonNull
    Completable sendRx(String textMessage);

    @NonNull
    Completable sendRx(UsedeskFileInfo usedeskFileInfo);

    @NonNull
    Completable sendRx(List<UsedeskFileInfo> usedeskFileInfoList);

    @NonNull
    Completable sendRx(UsedeskFeedback feedback);

    @NonNull
    Completable sendRx(UsedeskOfflineForm offlineForm);

    @NonNull
    Completable sendRx(UsedeskMessageButton messageButton);
}
