package ru.usedesk.chat_sdk.external;

import android.content.Context;

import io.reactivex.annotations.NonNull;
import ru.usedesk.chat_sdk.internal.di.InstanceBox;

public class UsedeskChatSdk {
    private static InstanceBox instanceBox;

    @NonNull
    public IUsedeskChatSdk initChat(@NonNull Context appContext) {
        if (instanceBox == null) {
            instanceBox = new InstanceBox(appContext);
        }
        return instanceBox.getUsedeskChatSdk();
    }

    @NonNull
    public IUsedeskChatSdk getChat() {
        if (instanceBox == null) {
            throw new RuntimeException("Must call UsedeskChatSdk.initChat(...) before");
        }
        return instanceBox.getUsedeskChatSdk();
    }

    public void releaseChat() {
        if (instanceBox != null) {
            instanceBox.release();
            instanceBox = null;
        }
    }
}
