package ru.usedesk.chat_sdk.external.service.notifications;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.service.notifications.view.UsedeskNotificationsService;

public class UsedeskNotificationsServiceFactory {

    public void stopService(@NonNull Context context) {
        Intent intent = new Intent(context, getServiceClass());
        UsedeskChatSdk.stopService(context, intent);
    }

    @NonNull
    protected Class<?> getServiceClass() {
        return UsedeskNotificationsService.class;
    }

    public void startService(@NonNull Context context) {
        Intent intent = new Intent(context, getServiceClass());
        UsedeskChatSdk.startService(context, intent);
    }
}
