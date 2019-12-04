package ru.usedesk.sdk.internal.domain.repositories.chat;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.OfflineForm;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.entity.exceptions.ApiException;
import ru.usedesk.sdk.internal.domain.entity.chat.OnMessageListener;

public interface IApiRepository {

    void setActionListener(@NonNull UsedeskActionListener actionListener);

    boolean post(UsedeskConfiguration configuration, OfflineForm offlineForm);

    void disconnect();

    boolean isConnected();

    void setSocket(String url) throws ApiException;

    void connect(OnMessageListener onMessageListener);

    void initChat(String token, UsedeskConfiguration usedeskConfiguration);

    void sendFeedbackMessage(String token, Feedback feedback);

    void sendMessageRequest(String token, String text, UsedeskFile usedeskFile);

    void sendUserEmail(String token, String email);
}