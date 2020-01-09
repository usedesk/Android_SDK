package ru.usedesk.chat_sdk.external;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.chat_sdk.internal.di.InstanceBox;

public class UsedeskChatSdk {
    private static InstanceBox instanceBox;
    private static UsedeskChatConfiguration configuration;

    @NonNull
    public static IUsedeskChatSdk init(@NonNull Context appContext,
                                       @NonNull UsedeskActionListener actionListener) {
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

    public static void startService(@NonNull Context context, @NonNull Intent intent) {
        checkConfiguration();
        configuration.serialize(intent);
        context.startService(intent);
    }

    public static void stopService(@NonNull Context context, @NonNull Intent intent) {
        context.stopService(intent);
    }

    private static void checkConfiguration() {
        if (configuration == null) {
            throw new RuntimeException("Call UsedeskChatSdk.setConfiguration(...) before");
        }
    }
}
