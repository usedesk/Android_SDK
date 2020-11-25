package ru.usedesk.chat_sdk.external.service.notifications;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.service.notifications.view.UsedeskNotificationsService;

public class UsedeskNotificationsServiceFactory {

    public void stopService(@NonNull Context context) {
        Intent intent = new Intent(context, getServiceClass());
        context.stopService(intent);
    }

    @NonNull
    protected Class<?> getServiceClass() {
        return UsedeskNotificationsService.class;
    }

    public void startService(@NonNull Context context, @NonNull UsedeskChatConfiguration usedeskChatConfiguration) {
        Intent intent = new Intent(context, getServiceClass());
        usedeskChatConfiguration.serialize(intent);
        context.startService(intent);
    }
}
