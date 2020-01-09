package ru.usedesk.chat_sdk.internal.di;

import android.content.Context;

import javax.inject.Inject;

import io.reactivex.annotations.NonNull;
import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskConfiguration;
import ru.usedesk.common_sdk.internal.appdi.InjectBox;

public class InstanceBox extends InjectBox {
    @Inject
    IUsedeskChatSdk usedeskChatSdk;

    public InstanceBox(@NonNull Context appContext, UsedeskConfiguration usedeskConfiguration) {
        init(new MainModule(appContext, usedeskConfiguration));
    }

    public IUsedeskChatSdk getUsedeskChatSdk() {
        return usedeskChatSdk;
    }
}
