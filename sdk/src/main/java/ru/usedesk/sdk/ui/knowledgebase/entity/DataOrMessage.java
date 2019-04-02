package ru.usedesk.sdk.ui.knowledgebase.entity;

public class DataOrMessage<T> {
    private T data;
    private Message messageState;

    public DataOrMessage(T data) {
        this.data = data;
        this.messageState = Message.NONE;
    }

    public DataOrMessage(Message messageState) {
        this.messageState = messageState;
    }

    public T getData() {
        return data;
    }

    public Message getMessage() {
        return messageState;
    }

    public enum Message {
        LOADING, ERROR, NONE;
    }
}
