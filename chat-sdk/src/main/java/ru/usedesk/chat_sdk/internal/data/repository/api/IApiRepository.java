package ru.usedesk.chat_sdk.internal.data.repository.api;

import androidx.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm;
import ru.usedesk.chat_sdk.internal.domain.entity.OnMessageListener;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public interface IApiRepository {

    void connect(@NonNull String url, @NonNull UsedeskActionListener actionListener, @NonNull OnMessageListener onMessageListener) throws UsedeskException;

    void init(@NonNull UsedeskChatConfiguration configuration, String token);

    void send(@NonNull String token, @NonNull String email, String name, Long phone, Long additionalId);

    void send(@NonNull UsedeskChatConfiguration configuration, @NonNull UsedeskOfflineForm offlineForm) throws UsedeskException;

    void send(@NonNull String token, @NonNull UsedeskFeedback feedback) throws UsedeskException;

    void send(@NonNull String token, @NonNull String text) throws UsedeskException;

    void send(@NonNull String token, @NonNull UsedeskFileInfo usedeskFileInfo) throws UsedeskException;

    void disconnect();
}