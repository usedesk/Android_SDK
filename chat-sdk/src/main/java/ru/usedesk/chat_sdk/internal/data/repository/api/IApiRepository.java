package ru.usedesk.chat_sdk.internal.data.repository.api;

import android.support.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.chat_sdk.external.entity.OnMessageListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskConfiguration;
import ru.usedesk.chat_sdk.external.entity.UsedeskFile;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException;


public interface IApiRepository {

    void setActionListener(@NonNull UsedeskActionListener actionListener);

    void post(UsedeskConfiguration configuration, OfflineForm offlineForm) throws UsedeskHttpException;

    void disconnect();

    boolean isConnected();

    void setSocket(String url) throws UsedeskSocketException;

    void connect(OnMessageListener onMessageListener);

    void initChat(String token, UsedeskConfiguration usedeskConfiguration);

    void sendFeedbackMessage(String token, Feedback feedback);

    void sendMessageRequest(String token, String text, UsedeskFile usedeskFile);

    void sendUserEmail(String token, String email, String name, Long phone, Long additionalId);
}