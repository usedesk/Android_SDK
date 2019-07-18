package ru.usedesk.sdk.external.service.notifications;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;
import ru.usedesk.sdk.external.service.notifications.view.UsedeskNotificationsService;

public class UsedeskNotificationsServiceFactory {
    public void startService(@NonNull Context context, @NonNull UsedeskConfiguration configuration) {
        Intent intent = new Intent(context, getServiceClass());
        configuration.serialize(intent);
        context.startService(intent);
    }

    public void stopService(@NonNull Context context) {
        Intent intent = new Intent(context, getServiceClass());
        context.stopService(intent);
    }

    @NonNull
    protected Class<?> getServiceClass() {
        return UsedeskNotificationsService.class;
    }
}
