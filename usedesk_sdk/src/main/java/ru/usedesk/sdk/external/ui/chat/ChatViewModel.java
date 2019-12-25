package ru.usedesk.sdk.external.ui.chat;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import ru.usedesk.sdk.external.UsedeskChat;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.chat.Feedback;
import ru.usedesk.sdk.external.entity.chat.UsedeskActionListenerRx;
import ru.usedesk.sdk.external.entity.chat.UsedeskFile;
import ru.usedesk.sdk.external.ui.mvi.MviViewModel;

public class ChatViewModel extends MviViewModel<ChatModel> {

    private UsedeskChat usedeskChat;

    private ChatViewModel(@NonNull Context context) {
        super(new ChatModel(true, false, new ArrayList<>(),
                0, new ArrayList<>(), null));

        UsedeskActionListenerRx actionListenerRx = new UsedeskActionListenerRx();

        addModelObservable(actionListenerRx.getConnectedObservable()
                .map(emptyItem -> new ChatModel.Builder()
                        .setLoading(false)
                        .build()));

        addModelObservable(actionListenerRx.getMessagesObservable()
                .map(messages -> new ChatModel.Builder()
                        .setMessages(messages)
                        .build()));

        addModelObservable(actionListenerRx.getOfflineFormExpectedObservable()
                .map(emptyItem -> new ChatModel.Builder()
                        .setOfflineFormExpected(true)
                        .build()));

        addModelObservable(actionListenerRx.getExceptionSubject()
                .map(exception -> {
                    /*if (exception instanceof UsedeskSocketException) {
                        switch (((UsedeskSocketException) exception).getError()) {
                            case DISCONNECTED:
                                break;
                            case FORBIDDEN_ERROR:
                                break;
                        }
                    } else if (exception instanceof UsedeskHttpException) {
                        switch (((UsedeskHttpException) exception).getError()) {
                            case IO_ERROR:
                                break;
                            case JSON_ERROR:
                                break;
                        }
                    }*/
                    return new ChatModel.Builder()
                            .setUsedeskException(exception)
                            .build();
                }));

        initLiveData(throwable -> {
            //nothing
        });

        usedeskChat = UsedeskSdk.initChat(context, actionListenerRx);
    }

    void setUsedeskFiles(List<UsedeskFile> usedeskFiles) {
        onNewModel(new ChatModel.Builder()
                .setUsedeskFiles(usedeskFiles)
                .build());
    }

    void sendMessage(String textMessage, List<UsedeskFile> usedeskFiles) {
        if (usedeskFiles != null && usedeskFiles.size() > 0) {
            asCompletable(() -> usedeskChat.sendMessage(textMessage, usedeskFiles));
        } else {
            asCompletable(() -> usedeskChat.sendTextMessage(textMessage));
        }
        onNewModel(new ChatModel.Builder()
                .setUsedeskFiles(new ArrayList<>())
                .build());
    }

    void sendFeedback(@NonNull Feedback feedback) {
        asCompletable(() -> usedeskChat.sendFeedbackMessage(feedback));
    }

    private void asCompletable(Runnable runnable) {
        Completable.create(emitter -> {
            runnable.run();
            emitter.onComplete();
        }).subscribe();
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        UsedeskSdk.releaseChat();
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
