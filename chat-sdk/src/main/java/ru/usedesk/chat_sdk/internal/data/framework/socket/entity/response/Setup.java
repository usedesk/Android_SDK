package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import ru.usedesk.sdk.external.entity.chat.Client;
import ru.usedesk.sdk.external.entity.chat.Message;

public class Setup {

    private boolean waitingEmail;
    private boolean noOperators;
    private Client client;
    private List<Message> messages;

    public Setup() {
        waitingEmail = true;
    }

    public boolean isWaitingEmail() {
        return waitingEmail || (client != null && client.getEmail() == null);
    }

    public boolean isNoOperators() {
        return noOperators;
    }

    public Client getClient() {
        return client;
    }

    public List<Message> getMessages() {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }

        List<Message> filteredMessages = new ArrayList<>();

        for (Message message : messages) {
            if (message.getChat() != null
                    && (!TextUtils.isEmpty(message.getText()) || message.getUsedeskFile() != null)) {
                filteredMessages.add(message);
            }
        }

        return filteredMessages;
    }
}