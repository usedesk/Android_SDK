package ru.usedesk.sdk.external.service.notifications.view;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.service.notifications.presenter.NotificationsModel;
import ru.usedesk.sdk.external.service.notifications.presenter.NotificationsPresenter;
import ru.usedesk.sdk.internal.appdi.ScopeChat;
import ru.usedesk.sdk.mvi.MviView;
import toothpick.Toothpick;

public abstract class UsedeskNotificationsService extends Service implements MviView<NotificationsModel> {

    private static final String NEW_MESSAGES_CHANNEL_ID = "newUsedeskMessages";
    private static final String MESSAGES_FROM_OPERATOR_CHANNEL_TITLE = "Messages from operator";

    @Inject
    NotificationsPresenter notificationsPresenter;

    private NotificationManager notificationManager;
    private Disposable messagesDisposable;

    public UsedeskNotificationsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        registerNotification();

        ScopeChat scopeChat = new ScopeChat(this, this);
        Toothpick.inject(this, scopeChat.getScope());
    }

    private void registerNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(getChannelId(),
                    getChannelTitle(), NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    protected NotificationManager getNotificationManager() {
        return notificationManager;
    }

    @NonNull
    protected String getChannelId() {
        return NEW_MESSAGES_CHANNEL_ID;
    }

    @NonNull
    protected String getChannelTitle() {
        return MESSAGES_FROM_OPERATOR_CHANNEL_TITLE;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            UsedeskConfiguration usedeskConfiguration = UsedeskConfiguration.deserialize(intent);

            UsedeskSdk.initChat(this, usedeskConfiguration, notificationsPresenter.getActionListenerRx());

            messagesDisposable = notificationsPresenter.getModelObservable()
                    .subscribe(this::renderModel);
        }

        return START_STICKY;
    }

    @Override
    public void renderModel(@NonNull NotificationsModel model) {
        showNotification(createNotification(model));
    }

    protected abstract void showNotification(@NonNull Notification notification);

    @Nullable
    protected PendingIntent getContentPendingIntent() {
        return null;
    }

    @Nullable
    protected PendingIntent getDeletePendingIntent() {
        return null;
    }

    @NonNull
    protected Notification createNotification(@NonNull NotificationsModel model) {
        String title = model.getMessage().getName();
        String text = model.getMessage().getText();

        if (model.getCount() > 1) {
            title += " (" + model.getCount() + ")";
        }

        Notification notification = new NotificationCompat.Builder(this, getChannelId())
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(getContentPendingIntent())
                .setDeleteIntent(getDeletePendingIntent())
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        return notification;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (messagesDisposable != null) {
            messagesDisposable.dispose();
        }

        UsedeskSdk.releaseChat();
    }
}
