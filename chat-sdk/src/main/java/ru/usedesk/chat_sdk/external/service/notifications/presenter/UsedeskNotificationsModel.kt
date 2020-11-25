package ru.usedesk.chat_sdk.external.service.notifications.presenter;

import androidx.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.UsedeskMessage;

public class UsedeskNotificationsModel {
    private final UsedeskMessage message;
    private final int count;

    UsedeskNotificationsModel() {
        this(null, 0);
    }

    UsedeskNotificationsModel(@NonNull UsedeskMessage message) {
        this(message, 1);
    }

    UsedeskNotificationsModel(UsedeskMessage message, int count) {
        this.message = message;
        this.count = count;
    }

    public UsedeskMessage getMessage() {
        return message;
    }

    public int getCount() {
        return count;
    }
}