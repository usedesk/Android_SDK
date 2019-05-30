package ru.usedesk.sdk.external.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.Message;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListener;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;

public class NotificationsService extends Service {
    public NotificationsService() {
    }

    public static void startService(@NonNull Context context,
                                    @NonNull UsedeskConfiguration usedeskConfiguration) {
        Intent intent = new Intent(context, NotificationsService.class);
        usedeskConfiguration.serialize(intent);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        UsedeskConfiguration usedeskConfiguration = UsedeskConfiguration.deserialize(intent);

        UsedeskSdk.initChat(this, usedeskConfiguration, new UsedeskActionListener() {
            @Override
            public void onConnected() {

            }

            @Override
            public void onMessageReceived(Message message) {
                //we have a new message
            }

            @Override
            public void onMessagesReceived(List<Message> messages) {

            }

            @Override
            public void onServiceMessageReceived(Message message) {

            }

            @Override
            public void onOfflineFormExpected() {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onError(int errorResId) {

            }

            @Override
            public void onError(Exception e) {

            }
        });

        return START_STICKY;
    }
}
