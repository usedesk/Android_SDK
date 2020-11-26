package ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.usedesk.chat_sdk.external.entity.UsedeskClient;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage;
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.PayloadMessage;

public class Setup {

    private boolean waitingEmail;
    private boolean noOperators;
    private UsedeskClient client;
    private List<PayloadMessage> messages;

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

    @NonNull
    public List<UsedeskMessage> getMessages() {
        if (messages == null) {
            return new ArrayList<>();
        }

        List<UsedeskMessage> filteredMessages = new ArrayList<>(messages.size());
        for (PayloadMessage payloadMessage : messages) {
            if (payloadMessage.getChat() != null
                    && (!TextUtils.isEmpty(payloadMessage.getText()) || payloadMessage.getFile() != null)) {
                filteredMessages.add(new UsedeskMessage(payloadMessage, payloadMessage.getPayload(), null));
            }
        }

        return filteredMessages;
    }
}