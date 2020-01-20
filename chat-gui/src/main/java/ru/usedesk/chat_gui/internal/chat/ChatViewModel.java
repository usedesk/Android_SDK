package ru.usedesk.chat_gui.internal.chat;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.chat_sdk.external.IUsedeskChatSdk;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.Feedback;
import ru.usedesk.chat_sdk.external.entity.OfflineForm;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;

public class ChatViewModel extends MviViewModel<ChatModel> {

    private IUsedeskChatSdk usedeskChat;

    ChatViewModel(@NonNull IUsedeskChatSdk usedeskChatSdk, @NonNull UsedeskActionListenerRx actionListenerRx) {
        super(new ChatModel(true, false, new ArrayList<>(),
                0, new ArrayList<>(), null));

        this.usedeskChat = usedeskChatSdk;

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
                .map(exception -> new ChatModel.Builder()
                        .setUsedeskException(exception)
                        .build()));

        initLiveData(throwable -> {
            //nothing
        });

        usedeskChat.connectRx()
                .subscribe();
    }

    public void setAttachedFileInfoList(@NonNull List<UsedeskFileInfo> usedeskFileInfoList) {
        onNewModel(new ChatModel.Builder()
                .setUsedeskFileInfoList(usedeskFileInfoList)
                .build());
    }

    public void sendFeedback(@NonNull Feedback feedback) {
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

    void detachFile(@NonNull UsedeskFileInfo usedeskFileInfo) {
        List<UsedeskFileInfo> attachedFileInfoList = new ArrayList<>(getLastModel().getUsedeskFileInfoList());
        attachedFileInfoList.remove(usedeskFileInfo);
        setAttachedFileInfoList(attachedFileInfoList);
    }

    void onSend(@NonNull String textMessage) {
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

    void onSend(@NonNull String name, @NonNull String email, @NonNull String message) {
        usedeskChat.sendRx(new OfflineForm(name, email, message))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}
