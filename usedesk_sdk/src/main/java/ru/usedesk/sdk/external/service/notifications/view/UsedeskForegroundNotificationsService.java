package ru.usedesk.sdk.external.service.notifications.view;

import android.app.Notification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

public abstract class UsedeskForegroundNotificationsService extends UsedeskNotificationsService {

    @Override
    public void onCreate() {
        super.onCreate();

        startForeground(getForegroundId(), createStartNotification());
    }

    @Override
    protected void showNotification(@NonNull Notification notification) {
        stopForeground(true);
        startForeground(getForegroundId(), notification);
    }

    protected abstract int getForegroundId();

    protected Notification createStartNotification() {
        String title = "Чат с оператором";

        Notification notification = new NotificationCompat.Builder(this, getChannelId())
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentIntent(getContentPendingIntent())
                .setDeleteIntent(getDeletePendingIntent())
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        return notification;
    }
}
