package ru.usedesk.sdk.domain.entity.chat;

import java.util.List;

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