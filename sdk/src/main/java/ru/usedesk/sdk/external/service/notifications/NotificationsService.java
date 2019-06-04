package ru.usedesk.sdk.external.service.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListenerRx;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.internal.appdi.ScopeChat;
import ru.usedesk.sdk.mvi.MviView;
import toothpick.Toothpick;

public class NotificationsService extends Service implements MviView<NotificationsModel> {

    private static final String NEW_MESSAGES_CHANNEL_ID = "newMessages";
    private static final String MESSAGES_FROM_OPERATOR_CHANNEL_TITLE = "Messages from operator";

    private NotificationsPresenter notificationsPresenter;
    private NotificationManager notificationManager;

    private Disposable messagesDisposable;

    public NotificationsService() {
    }

    public static void startService(@NonNull Context context,
                                    @NonNull UsedeskConfiguration usedeskConfiguration) {
        Intent intent = new Intent(context, NotificationsService.class);
        usedeskConfiguration.serialize(intent);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationsPresenter = new NotificationsPresenter(new UsedeskActionListenerRx());

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        registerNotification();

        ScopeChat scopeChat = new ScopeChat(notificationsPresenter, this);
        Toothpick.inject(notificationsPresenter, scopeChat.getScope());
    }

    private void registerNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(NEW_MESSAGES_CHANNEL_ID,
                    MESSAGES_FROM_OPERATOR_CHANNEL_TITLE, NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        UsedeskConfiguration usedeskConfiguration = UsedeskConfiguration.deserialize(intent);

        UsedeskSdk.initChat(this, usedeskConfiguration, notificationsPresenter.getActionListenerRx());

        messagesDisposable = notificationsPresenter.getModelObservable()
                .subscribe(this::renderModel);

        return START_STICKY;
    }

    @Override
    public void renderModel(@NonNull NotificationsModel model) {
        String title = "Operator: " + model.getMessage().getOperator();
        String text = model.getCount() == 1
                ? "Message: " + model.getMessage().getText()
                : model.getCount() + " messages";

        Notification notification = new NotificationCompat.Builder(this, NEW_MESSAGES_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle(title)
                .setContentText(text)
                .build();

        notificationManager.notify(1, notification);
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
