package ru.usedesk.chat_gui.screens.chat;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;

public class ChatViewModel extends MviViewModel<ChatModel> {

    private IUsedeskChatSdk usedeskChat;

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

        usedeskChat = UsedeskChatSdk.init(context, actionListenerRx);
    }

    void setAttachedFileInfoList(List<UsedeskFileInfo> usedeskFileInfoList) {
        onNewModel(new ChatModel.Builder()
                .setUsedeskFileInfoList(usedeskFileInfoList)
                .build());
    }

    void sendFeedback(@NonNull Feedback feedback) {
        usedeskChat.sendRx(feedback)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        UsedeskChatSdk.release();
    }

    public void detachFile(@NonNull UsedeskFileInfo usedeskFileInfo) {
        List<UsedeskFileInfo> attachedFileInfoList = new ArrayList<>(getLastModel().getUsedeskFileInfoList());
        attachedFileInfoList.remove(usedeskFileInfo);
        setAttachedFileInfoList(attachedFileInfoList);
    }

    public void onSend(@NonNull String textMessage) {
        usedeskChat.sendRx(textMessage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        List<UsedeskFileInfo> usedeskFileInfoList = getLastModel().getUsedeskFileInfoList();
        usedeskChat.sendRx(usedeskFileInfoList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

        onNewModel(new ChatModel.Builder()
                .setUsedeskFileInfoList(new ArrayList<>())
                .build());
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
