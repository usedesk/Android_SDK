package ru.usedesk.sdk.external.ui.chat;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import ru.usedesk.sdk.external.AppSession;
import ru.usedesk.sdk.external.UsedeskChat;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListenerRx;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.ui.UsedeskViewModel;

public class ChatViewModel extends UsedeskViewModel<ChatModel> {
    private UsedeskChat usedeskChat;
    private UsedeskActionListenerRx actionListenerRx;
    private CompositeDisposable disposables;

    public ChatViewModel(@NonNull Context context) {

        disposables = new CompositeDisposable();

        actionListenerRx = new UsedeskActionListenerRx();

        disposables.add(actionListenerRx.getConnectedObservable()
                .subscribe(emptyItem ->
                        onNewModel(new ChatModel.Builder()
                                .setLoading(true)
                                .build())));

        disposables.add(actionListenerRx.getMessagesObservable()
                .subscribe(messages ->
                        onNewModel(new ChatModel.Builder()
                                .setMessages(messages)
                                .build())));

        disposables.add(actionListenerRx.getOfflineFormExpectedObservable()
                .subscribe(emptyItem ->
                        onNewModel(new ChatModel.Builder()
                                .setOfflineFormExpected(true)
                                .build())));

        disposables.add(actionListenerRx.getErrorResIdSubject()
                .subscribe(errorId ->
                        onNewModel(new ChatModel.Builder()
                                .setErrorId(errorId)
                                .build())));

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

    void sendFeedback(Feedback feedback) {
        usedeskChat.sendFeedbackMessage(feedback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposables.dispose();
    }
}
