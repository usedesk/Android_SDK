package ru.usedesk.chat_sdk.external.entity;

import androidx.annotation.NonNull;

import java.util.List;

import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public interface IUsedeskActionListener {

    void onConnected();

    void onMessageReceived(UsedeskMessage message);

    void onMessagesReceived(List<UsedeskMessage> messages);

    void onFeedbackReceived();

    void onOfflineFormExpected();

    void onDisconnected();

    void onException(@NonNull UsedeskException usedeskException);
}