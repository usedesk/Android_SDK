package ru.usedesk.chat_sdk.external.service.notifications;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import ru.usedesk.chat_sdk.external.service.notifications.view.UsedeskNotificationsService;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.UsedeskConfiguration;

public class UsedeskNotificationsServiceFactory {
    @Deprecated
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

    public void startService(@NonNull Context context) {
        UsedeskConfiguration usedeskConfiguration = UsedeskSdk.getUsedeskConfiguration();
        if (usedeskConfiguration == null) {
            throw new RuntimeException("Need to set UsedeskConfiguration");
        }
        startService(context, usedeskConfiguration);
    }
}
