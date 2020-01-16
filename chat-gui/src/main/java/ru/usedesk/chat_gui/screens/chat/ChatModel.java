package ru.usedesk.chat_gui.screens.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ru.usedesk.chat_sdk.external.entity.Message;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public class ChatModel extends ReducibleModel<ChatModel> {
    private Boolean loading;

    private Boolean offlineFormExpected = false;

    private List<Message> messages;
    private Integer messagesCountDif = 0;

    private List<UsedeskFileInfo> usedeskFileInfoList;

    private UsedeskException usedeskException;

    private ChatModel() {
    }

    public ChatModel(@NonNull Boolean loading, @NonNull Boolean offlineFormExpected,
                     @NonNull List<Message> messages, @NonNull Integer messagesCountDif,
                     @NonNull List<UsedeskFileInfo> usedeskFileInfoList, @Nullable UsedeskException usedeskException) {
        this.loading = loading;
        this.offlineFormExpected = offlineFormExpected;
        this.messages = messages;
        this.messagesCountDif = messagesCountDif;
        this.usedeskFileInfoList = usedeskFileInfoList;
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
    public List<UsedeskFileInfo> getUsedeskFileInfoList() {
        return usedeskFileInfoList;
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
                .setLoading(ReducibleModel.reduce(this.loading, newModel.loading))
                .setOfflineFormExpected(newModel.offlineFormExpected)
                .setMessages(ReducibleModel.reduce(this.messages, newModel.messages))
                .setMessagesCountDif(newModel.messagesCountDif)
                .setUsedeskFileInfoList(ReducibleModel.reduce(this.usedeskFileInfoList, newModel.usedeskFileInfoList))
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

        public Builder setUsedeskFileInfoList(List<UsedeskFileInfo> usedeskFileInfoList) {
            chatModel.usedeskFileInfoList = usedeskFileInfoList;
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
