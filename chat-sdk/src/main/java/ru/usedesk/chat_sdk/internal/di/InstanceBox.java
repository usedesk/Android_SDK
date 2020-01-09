package ru.usedesk.chat_sdk.internal.di;

import android.content.Context;

import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.common_sdk.internal.appdi.InjectBox;

public class InstanceBox extends InjectBox {
    @Inject
    IUsedeskChatSdk usedeskChatSdk;

    public InstanceBox(@NonNull Context appContext, @NonNull UsedeskChatConfiguration usedeskChatConfiguration,
                       @NonNull UsedeskActionListener actionListener) {
        init(new MainModule(appContext, usedeskChatConfiguration, actionListener));
    }

    public IUsedeskChatSdk getUsedeskChatSdk() {
        return usedeskChatSdk;
    }

    @Override
    public void release() {
        usedeskChatSdk.disconnect();
        super.release();
    }
}
