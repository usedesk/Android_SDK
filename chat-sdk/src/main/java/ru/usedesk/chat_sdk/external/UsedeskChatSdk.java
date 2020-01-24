package ru.usedesk.chat_sdk.external;

import android.content.Context;

import androidx.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.external.service.notifications.UsedeskNotificationsServiceFactory;
import ru.usedesk.chat_sdk.internal.di.InstanceBox;

public class UsedeskChatSdk {
    private static InstanceBox instanceBox;
    private static UsedeskChatConfiguration configuration;
    private static UsedeskNotificationsServiceFactory notificationsServiceFactory = new UsedeskNotificationsServiceFactory();

    @NonNull
    public static IUsedeskChatSdk init(@NonNull Context appContext,
                                       @NonNull IUsedeskActionListener actionListener) {
        if (instanceBox == null) {
            checkConfiguration();
            instanceBox = new InstanceBox(appContext, configuration, actionListener);
        }
        return instanceBox.getUsedeskChatSdk();
    }

    @NonNull
    public static IUsedeskChatSdk getInstance() {
        if (instanceBox == null) {
            throw new RuntimeException("Must call UsedeskChatSdk.initChat(...) before");
        }
        return instanceBox.getUsedeskChatSdk();
    }

    public static void release() {
        if (instanceBox != null) {
            instanceBox.release();
            instanceBox = null;
        }
    }

    public static void setConfiguration(@NonNull UsedeskChatConfiguration usedeskChatConfiguration) {
        configuration = usedeskChatConfiguration;
    }

    public static void startService(@NonNull Context context) {
        checkConfiguration();
        notificationsServiceFactory.startService(context, configuration);
    }

    public static void stopService(@NonNull Context context) {
        notificationsServiceFactory.stopService(context);
    }

    private static void checkConfiguration() {
        if (configuration == null) {
            throw new RuntimeException("Call UsedeskChatSdk.setConfiguration(...) before");
        }
    }

    public static void setNotificationsServiceFactory(@NonNull UsedeskNotificationsServiceFactory usedeskNotificationsServiceFactory) {
        notificationsServiceFactory = usedeskNotificationsServiceFactory;
    }
}
