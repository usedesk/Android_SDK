package ru.usedesk.sdk.ui.knowledgebase.pages;

import java.util.List;

public class ListOrMessage<T> {
    private List<T> dataList;
    private Message messageState;

    public ListOrMessage(List<T> dataList) {
        this.dataList = dataList;
        this.messageState = Message.NONE;
    }

    public ListOrMessage(Message messageState) {
        this.messageState = messageState;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public Message getMessage() {
        return messageState;
    }

    public enum Message {
        LOADING, ERROR, NONE;
    }
}
