package ru.usedesk.chat_gui.internal.chat;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

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
