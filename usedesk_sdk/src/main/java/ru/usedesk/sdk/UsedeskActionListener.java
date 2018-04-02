package ru.usedesk.sdk;

import java.util.List;

import ru.usedesk.sdk.models.Message;

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