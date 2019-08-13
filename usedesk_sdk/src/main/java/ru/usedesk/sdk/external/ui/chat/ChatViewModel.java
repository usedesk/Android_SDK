package ru.usedesk.sdk.external.ui.chat;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.external.AppSession;
import ru.usedesk.sdk.external.UsedeskChat;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListenerRx;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.ui.MviViewModel;

public class ChatViewModel extends MviViewModel<ChatModel> {

    private UsedeskChat usedeskChat;
    private UsedeskActionListenerRx actionListenerRx;
    private Disposable disposable;

    public ChatViewModel(@NonNull Context context) {
        super(new ChatModel(true, false, new ArrayList<>(),
                0, 0, null));

        actionListenerRx = new UsedeskActionListenerRx();

        disposable = Observable.merge(
                actionListenerRx.getConnectedObservable()
                        .map(emptyItem -> new ChatModel.Builder()
                                .setLoading(false)
                                .build()),
                actionListenerRx.getMessagesObservable()
                        .map(messages -> new ChatModel.Builder()
                                .setMessages(messages)
                                .build()),
                actionListenerRx.getOfflineFormExpectedObservable()
                        .map(emptyItem -> new ChatModel.Builder()
                                .setOfflineFormExpected(true)
                                .build()),
                actionListenerRx.getErrorResIdSubject()
                        .map(errorId -> new ChatModel.Builder()
                                .setErrorId(errorId)
                                .build()))
                .subscribe(this::onNewModel);

        usedeskChat = UsedeskSdk.initChat(context,
                AppSession.getSession().getUsedeskConfiguration(), actionListenerRx);
    }

    void sendMessage(String textMessage, List<UsedeskFile> usedeskFiles) {
        if (usedeskFiles != null) {
            usedeskChat.sendMessage(textMessage, usedeskFiles);
        } else {
            usedeskChat.sendTextMessage(textMessage);
        }
    }

    void sendFeedback(@NonNull Feedback feedback) {
        usedeskChat.sendFeedbackMessage(feedback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private Context context;

        public Factory(@NonNull Context context) {
            this.context = context.getApplicationContext();
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked cast")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ChatViewModel(context);
        }
    }
}
