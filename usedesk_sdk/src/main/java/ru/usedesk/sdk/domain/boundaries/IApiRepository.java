package ru.usedesk.sdk.domain.boundaries;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.domain.entity.Feedback;
import ru.usedesk.sdk.domain.entity.OfflineForm;
import ru.usedesk.sdk.domain.entity.OnMessageListener;
import ru.usedesk.sdk.domain.entity.UsedeskActionListener;
import ru.usedesk.sdk.domain.entity.UsedeskConfiguration;
import ru.usedesk.sdk.domain.entity.UsedeskFile;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;

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