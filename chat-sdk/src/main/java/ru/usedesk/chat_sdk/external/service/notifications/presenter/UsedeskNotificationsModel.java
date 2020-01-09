package ru.usedesk.chat_sdk.external.service.notifications.presenter;

import android.support.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.Message;

public class UsedeskNotificationsModel {
    private final Message message;
    private final int count;

    UsedeskNotificationsModel() {
        this(null, 0);
    }

    UsedeskNotificationsModel(@NonNull Message message) {
        this(message, 1);
    }

    UsedeskNotificationsModel(Message message, int count) {
        this.message = message;
        this.count = count;
    }

    public Message getMessage() {
        return message;
    }

    public int getCount() {
        return count;
    }
}