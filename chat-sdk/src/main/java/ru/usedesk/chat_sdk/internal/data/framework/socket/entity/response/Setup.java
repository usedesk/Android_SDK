package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import ru.usedesk.chat_sdk.external.entity.UsedeskClient;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage;

public class Setup {

    private boolean waitingEmail;
    private boolean noOperators;
    private UsedeskClient client;
    private List<UsedeskMessage> messages;

    public Setup() {
        waitingEmail = true;
    }

    public boolean isWaitingEmail() {
        return waitingEmail || (client != null && client.getEmail() == null);
    }

    public boolean isNoOperators() {
        return noOperators;
    }

    public UsedeskClient getClient() {
        return client;
    }

    public List<UsedeskMessage> getMessages() {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }

        List<UsedeskMessage> filteredMessages = new ArrayList<>();

        for (UsedeskMessage message : messages) {
            if (message.getChat() != null
                    && (!TextUtils.isEmpty(message.getText()) || message.getUsedeskFile() != null)) {
                filteredMessages.add(message);
            }
        }

        return filteredMessages;
    }
}