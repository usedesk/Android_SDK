package ru.usedesk.chat_gui.screens.chat;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx;

public class ChatViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public ChatViewModelFactory(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked cast")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        UsedeskActionListenerRx actionListenerRx = new UsedeskActionListenerRx();
        IUsedeskChatSdk usedeskChatSdk = UsedeskChatSdk.init(context, actionListenerRx);
        return (T) new ChatViewModel(usedeskChatSdk, actionListenerRx);
    }
}
