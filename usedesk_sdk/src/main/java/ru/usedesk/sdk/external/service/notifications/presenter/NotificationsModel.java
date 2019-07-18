package ru.usedesk.sdk.external.service.notifications.presenter;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.external.entity.chat.Message;

public class NotificationsModel {
    private final Message message;
    private final int count;

    NotificationsModel() {
        this(null, 0);
    }

    NotificationsModel(@NonNull Message message) {
        this(message, 1);
    }

    NotificationsModel(Message message, int count) {
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