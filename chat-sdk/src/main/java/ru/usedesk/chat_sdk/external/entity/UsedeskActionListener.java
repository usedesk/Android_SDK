package ru.usedesk.chat_sdk.external.entity;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public interface UsedeskActionListener {

    void onConnected();

    void onMessageReceived(Message message);

    void onMessagesReceived(List<Message> messages);

    void onServiceMessageReceived(Message message);

    void onOfflineFormExpected();

    void onDisconnected();

    void onException(@NonNull UsedeskException usedeskException);
}