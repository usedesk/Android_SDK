package ru.usedesk.chat_sdk.external;

import androidx.annotation.NonNull;

import java.util.List;

import io.reactivex.Completable;
import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.MessageButtons;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public interface IUsedeskChatSdk {
    void connect() throws UsedeskException;

    void disconnect() throws UsedeskException;

    void send(String textMessage) throws UsedeskException;

    void send(UsedeskFileInfo usedeskFileInfo) throws UsedeskException;

    void send(List<UsedeskFileInfo> usedeskFileInfoList) throws UsedeskException;

    void send(Feedback feedback) throws UsedeskException;

    void send(OfflineForm offlineForm) throws UsedeskException;

    void send(MessageButtons.MessageButton messageButton) throws UsedeskException;

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
    Completable sendRx(Feedback feedback);

    @NonNull
    Completable sendRx(OfflineForm offlineForm);

    @NonNull
    Completable sendRx(MessageButtons.MessageButton messageButton);
}
