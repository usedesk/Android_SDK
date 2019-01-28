package ru.usedesk.sdk.domain.entity;

import java.util.List;

import ru.usedesk.sdk.domain.entity.Message;

public interface UsedeskActionListener {

    void onConnected();

    void onMessageReceived(Message message);

    void onMessagesReceived(List<Message> messages);

    void onServiceMessageReceived(Message message);

    void onOfflineFormExpected();

    void onDisconnected();

    void onError(int errorResId);

    void onError(Exception e);
}