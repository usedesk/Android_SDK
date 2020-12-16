package ru.usedesk.chat_sdk.external.service.notifications.view;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import io.reactivex.disposables.Disposable;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.service.notifications.presenter.UsedeskNotificationsModel;
import ru.usedesk.chat_sdk.external.service.notifications.presenter.UsedeskNotificationsPresenter;

public abstract class UsedeskNotificationsService extends Service {

    private static final String NEW_MESSAGES_CHANNEL_ID = "newUsedeskMessages";
    private static final String MESSAGES_FROM_OPERATOR_CHANNEL_TITLE = "Messages from operator";

    private final UsedeskNotificationsPresenter presenter;

    private NotificationManager notificationManager;
    private Disposable messagesDisposable;

    public UsedeskNotificationsService() {
        presenter = new UsedeskNotificationsPresenter();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        registerNotification();
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
            UsedeskChatSdk.release();
            UsedeskChatConfiguration configuration = UsedeskChatConfiguration.deserialize(intent);
            UsedeskChatSdk.setConfiguration(configuration);
            UsedeskChatSdk.init(this, presenter.getActionListener());

            presenter.init();

            messagesDisposable = presenter.getModelObservable()
                    .subscribe(this::renderModel);
        }

        return Service.START_STICKY;
    }

    private void renderModel(@NonNull UsedeskNotificationsModel model) {
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
    protected Notification createNotification(@NonNull UsedeskNotificationsModel model) {
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

        UsedeskChatSdk.release();

        Log.d("TAG", "ServiceOnDestroy");
    }
}
