package ru.usedesk.sdk.external.ui.chat;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.external.entity.chat.Message;

public class ChatModel {
    private boolean loading;

    private boolean offlineFormExpected;

    private List<Message> messages;
    private int messagesCountDif;

    private int errorId;
    private Exception exception;

    private ChatModel() {
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isOfflineFormExpected() {
        return offlineFormExpected;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int getMessagesCountDif() {
        return messagesCountDif;
    }

    public int getErrorId() {
        return errorId;
    }

    public Exception getException() {
        return exception;
    }

    public static class Builder {
        private ChatModel chatModel = new ChatModel();

        public Builder() {
        }

        public Builder setLoading(boolean loading) {
            chatModel.loading = loading;
            return this;
        }

        public Builder setOfflineFormExpected(boolean offlineFormExpected) {
            chatModel.offlineFormExpected = offlineFormExpected;
            return this;
        }

        public Builder setMessages(@NonNull List<Message> messages) {
            chatModel.messages = messages;
            return this;
        }

        public Builder setMessages(@NonNull List<Message> messages,
                                   int messagesCountDif) {
            chatModel.messages = messages;
            chatModel.messagesCountDif = messagesCountDif;
            return this;
        }

        public Builder setErrorId(int errorId) {
            chatModel.errorId = errorId;
            return this;
        }

        public Builder setException(@NonNull Exception exception) {
            chatModel.exception = exception;
            return this;
        }

        public ChatModel build() {
            return chatModel;
        }
    }
}
