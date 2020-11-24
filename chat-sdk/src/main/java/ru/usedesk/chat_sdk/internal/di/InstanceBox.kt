package ru.usedesk.chat_sdk.internal.di;

import android.content.Context;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.chat_sdk.external.IUsedeskChat;
import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener;
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration;
import ru.usedesk.common_sdk.internal.appdi.InjectBox;

@SuppressWarnings("injectable")
public class InstanceBox extends InjectBox {
    @Inject
    IUsedeskChat usedeskChatSdk;

    public InstanceBox(@NonNull Context appContext,
                       @NonNull UsedeskChatConfiguration usedeskChatConfiguration,
                       @NonNull IUsedeskActionListener actionListener) {
        init(new MainModule(appContext, usedeskChatConfiguration, actionListener));
    }

    public IUsedeskChat getUsedeskChatSdk() {
        return usedeskChatSdk;
    }

    @Override
    public void release() {
        usedeskChatSdk.disconnectRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        super.release();
    }
}
