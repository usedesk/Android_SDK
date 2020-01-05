package ru.usedesk.chat_sdk.external;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.OfflineForm;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.sdk.external.entity.exceptions.UsedeskSocketException;
import ru.usedesk.sdk.internal.domain.entity.chat.OnMessageListener;

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