package ru.usedesk.sdk.external.ui.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.usedesk.sdk.external.entity.chat.Message;
import ru.usedesk.sdk.external.ui.Reducable;

public class ChatModel extends Reducable<ChatModel> {
    private Boolean loading;

    private Boolean offlineFormExpected;

    private List<Message> messages;
    private Integer messagesCountDif;

    private Integer errorId;
    private Exception exception;

    private ChatModel() {
    }

    public ChatModel(@NonNull Boolean loading, @NonNull Boolean offlineFormExpected,
                     @NonNull List<Message> messages, @NonNull Integer messagesCountDif,
                     @Nullable Integer errorId, @Nullable Exception exception) {
        this.loading = loading;
        this.offlineFormExpected = offlineFormExpected;
        this.messages = messages;
        this.messagesCountDif = messagesCountDif;
        this.errorId = errorId;
        this.exception = exception;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isOfflineFormExpected() {
        return offlineFormExpected;
    }

    @NonNull
    public List<Message> getMessages() {
        return messages;
    }

    public int getMessagesCountDif() {
        return messagesCountDif;
    }

    @Nullable
    public Integer getErrorId() {
        return errorId;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

    @Override
    @NonNull
    public ChatModel reduce(@NonNull ChatModel newModel) {
        if (newModel.messages != null) {
            newModel.messagesCountDif = newModel.messages.size() - this.messages.size();
        }

        return new ChatModel.Builder()
                .setLoading(reduceValue(this.loading, newModel.loading))
                .setOfflineFormExpected(reduceValue(this.offlineFormExpected, newModel.offlineFormExpected))
                .setMessages(reduceValue(this.messages, newModel.messages))
                .setMessagesCountDif(reduceValue(this.messagesCountDif, newModel.messagesCountDif))
                .setErrorId(reduceValue(this.errorId, newModel.errorId))
                .setException(reduceValue(this.exception, newModel.exception))
                .build();
    }

    public static class Builder {
        private ChatModel chatModel = new ChatModel();

        public Builder() {
        }

        public Builder setLoading(Boolean loading) {
            chatModel.loading = loading;
            return this;
        }

        public Builder setOfflineFormExpected(Boolean offlineFormExpected) {
            chatModel.offlineFormExpected = offlineFormExpected;
            return this;
        }

        public Builder setMessages(List<Message> messages) {
            chatModel.messages = messages;
            return this;
        }

        public Builder setMessagesCountDif(int messagesCountDif) {
            chatModel.messagesCountDif = messagesCountDif;
            return this;
        }

        public Builder setErrorId(Integer errorId) {
            chatModel.errorId = errorId;
            return this;
        }

        public Builder setException(Exception exception) {
            chatModel.exception = exception;
            return this;
        }

        public ChatModel build() {
            return chatModel;
        }
    }
}
