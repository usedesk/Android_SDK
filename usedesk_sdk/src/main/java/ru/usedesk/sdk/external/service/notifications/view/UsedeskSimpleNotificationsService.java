package ru.usedesk.sdk.external.service.notifications.view;

import android.app.Notification;
import android.support.annotation.NonNull;

public abstract class UsedeskSimpleNotificationsService extends UsedeskNotificationsService {
    @Override
    protected void showNotification(@NonNull Notification notification) {
        getNotificationManager().notify(5, notification);
    }
}
