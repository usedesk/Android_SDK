package ru.usedesk.chat_sdk.external;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.usedesk.chat_sdk.external.entity.UsedeskConfiguration;
import ru.usedesk.chat_sdk.internal.di.InstanceBox;

public class UsedeskChatSdk {
    private static InstanceBox instanceBox;

    @NonNull
    public IUsedeskChatSdk init(@NonNull Context appContext,
                                @NonNull UsedeskConfiguration usedeskConfiguration) {
        if (instanceBox == null) {
            instanceBox = new InstanceBox(appContext, usedeskConfiguration);
        }
        return instanceBox.getUsedeskChatSdk();
    }

    @NonNull
    public IUsedeskChatSdk getInstance() {
        if (instanceBox == null) {
            throw new RuntimeException("Must call UsedeskChatSdk.initChat(...) before");
        }
        return instanceBox.getUsedeskChatSdk();
    }

    public void release() {
        if (instanceBox != null) {
            instanceBox.release();
            instanceBox = null;
        }
    }
}
