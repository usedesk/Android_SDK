package ru.usedesk.sdk.external.ui.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.usedesk.sdk.external.entity.chat.Message;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.ui.mvi.ReducableModel;

public class ChatModel extends ReducableModel<ChatModel> {
    private Boolean loading;

    private Boolean offlineFormExpected = false;

    private List<Message> messages;
    private Integer messagesCountDif = 0;

    private List<UsedeskFile> usedeskFiles;

    private Integer errorId;
    private Exception exception;

    private ChatModel() {
    }

    public ChatModel(@NonNull Boolean loading, @NonNull Boolean offlineFormExpected,
                     @NonNull List<Message> messages, @NonNull Integer messagesCountDif,
                     @NonNull List<UsedeskFile> usedeskFiles,
                     @Nullable Integer errorId, @Nullable Exception exception) {
        this.loading = loading;
        this.offlineFormExpected = offlineFormExpected;
        this.messages = messages;
        this.messagesCountDif = messagesCountDif;
        this.usedeskFiles = usedeskFiles;
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

    @NonNull
    public List<UsedeskFile> getUsedeskFiles() {
        return usedeskFiles;
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
                .setLoading(reduce(this.loading, newModel.loading))
                .setOfflineFormExpected(newModel.offlineFormExpected)
                .setMessages(reduce(this.messages, newModel.messages))
                .setMessagesCountDif(newModel.messagesCountDif)
                .setUsedeskFiles(reduce(this.usedeskFiles, newModel.usedeskFiles))
                .setErrorId(newModel.errorId)
                .setException(newModel.exception)
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

        public Builder setUsedeskFiles(List<UsedeskFile> usedeskFiles) {
            chatModel.usedeskFiles = usedeskFiles;
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
