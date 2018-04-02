package ru.usedesk.sdk.models;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class Setup {

    private boolean waitingEmail;
    private boolean noOperators;
    private Client client;
    private List<Message> messages;

    public Setup() {
        waitingEmail = true;
    }

    public boolean isWaitingEmail() {
        return waitingEmail;
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
                    && !TextUtils.isEmpty(message.getText())) {
                filteredMessages.add(message);
            }
        }

        return filteredMessages;
    }
}