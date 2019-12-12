package ru.usedesk.sdk.external.ui.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import ru.usedesk.sdk.external.entity.chat.Message;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.entity.exceptions.UsedeskException;
import ru.usedesk.sdk.external.ui.mvi.ReducibleModel;

public class ChatModel extends ReducibleModel<ChatModel> {
    private Boolean loading;

    private Boolean offlineFormExpected = false;

    private List<Message> messages;
    private Integer messagesCountDif = 0;

    private List<UsedeskFile> usedeskFiles;

    private UsedeskException usedeskException;

    private ChatModel() {
    }

    public ChatModel(@NonNull Boolean loading, @NonNull Boolean offlineFormExpected,
                     @NonNull List<Message> messages, @NonNull Integer messagesCountDif,
                     @NonNull List<UsedeskFile> usedeskFiles, @Nullable UsedeskException usedeskException) {
        this.loading = loading;
        this.offlineFormExpected = offlineFormExpected;
        this.messages = messages;
        this.messagesCountDif = messagesCountDif;
        this.usedeskFiles = usedeskFiles;
        this.usedeskException = usedeskException;
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
    public Exception getUsedeskException() {
        return usedeskException;
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
                .setUsedeskException(newModel.usedeskException)
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

        public Builder setUsedeskException(UsedeskException exception) {
            chatModel.usedeskException = exception;
            return this;
        }

        public ChatModel build() {
            return chatModel;
        }
    }
}
