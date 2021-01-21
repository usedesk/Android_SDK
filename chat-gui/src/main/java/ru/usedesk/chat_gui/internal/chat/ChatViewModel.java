package ru.usedesk.chat_gui.internal.chat;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import ru.usedesk.chat_sdk.external.IUsedeskChat;
import ru.usedesk.chat_sdk.external.UsedeskChatSdk;
import ru.usedesk.chat_sdk.external.entity.UsedeskActionListenerRx;
import ru.usedesk.chat_sdk.external.entity.UsedeskFeedback;
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo;
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage;
import ru.usedesk.chat_sdk.external.entity.UsedeskOfflineForm;
import ru.usedesk.chat_sdk.external.entity.UsedeskSingleLifeEvent;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public class ChatViewModel extends ViewModel {

    private final IUsedeskChat usedeskChat;

    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Set<Integer>> feedbacksLiveData = new MutableLiveData<>();
    private final MutableLiveData<UsedeskException> exceptionLiveData = new MutableLiveData<>();
    private final MutableLiveData<MessagePanelState> messagePanelStateLiveData = new MutableLiveData<>(MessagePanelState.MESSAGE_PANEL);
    private final MutableLiveData<List<UsedeskMessage>> messagesLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<UsedeskFileInfo>> fileInfoListLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>("");
    private final MutableLiveData<String> nameLiveData = new MutableLiveData<>("");
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>("");
    private final MutableLiveData<UsedeskSingleLifeEvent<Object>> largeFileSizeErrorLiveData = new MutableLiveData<>();

    ChatViewModel(@NonNull IUsedeskChat usedeskChat, @NonNull UsedeskActionListenerRx actionListenerRx) {
        this.usedeskChat = usedeskChat;

        clearFileInfoList();

        toLiveData(actionListenerRx.getMessagesObservable(), messagesLiveData);
        toLiveData(actionListenerRx.getOfflineFormExpectedObservable()
                .map(configuration -> {
                    nameLiveData.postValue(configuration.getClientName());
                    emailLiveData.postValue(configuration.getEmail());
                    return MessagePanelState.OFFLINE_FORM_EXPECTED;
                }), messagePanelStateLiveData);
        toLiveData(actionListenerRx.getExceptionObservable(), exceptionLiveData);

        disposables.add(actionListenerRx.getConnectedStateSubject()
                .subscribe(connected -> {
                    if (!connected) {
                        justComplete(this.usedeskChat.connectRx());
                    }
                }));

        feedbacksLiveData.setValue(new HashSet<>());
    }

    void onMessageChanged(@NonNull String message) {
        messageLiveData.setValue(message);
    }

    private void clearFileInfoList() {
        fileInfoListLiveData.setValue(new ArrayList<>());
    }

    private void justComplete(@NonNull Completable completable) {
        justComplete(completable, Throwable::printStackTrace);
    }

    private void justComplete(@NonNull Completable completable,
                              @NonNull Consumer<Throwable> throwableConsumer) {
        addDisposable(completable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                }, throwableConsumer));
    }

    private <OUT extends IN, IN> void toLiveData(@NonNull Observable<OUT> observable, @NonNull MutableLiveData<IN> liveData) {
        addDisposable(observable.subscribe(liveData::postValue, Throwable::printStackTrace));
    }

    private void addDisposable(@NonNull Disposable disposable) {
        disposables.add(disposable);
    }

    @NonNull
    public LiveData<Set<Integer>> getFeedbacksLiveData() {
        return feedbacksLiveData;
    }

    @NonNull
    public LiveData<UsedeskException> getExceptionLiveData() {
        return exceptionLiveData;
    }

    @NonNull
    LiveData<MessagePanelState> getMessagePanelStateLiveData() {
        return messagePanelStateLiveData;
    }

    @NonNull
    LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    @NonNull
    LiveData<String> getNameLiveData() {
        return nameLiveData;
    }

    @NonNull
    LiveData<String> getEmailLiveData() {
        return emailLiveData;
    }

    @NonNull
    public LiveData<UsedeskSingleLifeEvent<Object>> getLargeFileSizeErrorLiveData() {
        return largeFileSizeErrorLiveData;
    }

    @NonNull
    public LiveData<List<UsedeskMessage>> getMessagesLiveData() {
        return messagesLiveData;
    }

    @NonNull
    public LiveData<List<UsedeskFileInfo>> getFileInfoListLiveData() {
        return fileInfoListLiveData;
    }

    public void setAttachedFileInfoList(@NonNull List<UsedeskFileInfo> usedeskFileInfoList) {
        fileInfoListLiveData.postValue(usedeskFileInfoList);
    }

    @SuppressWarnings("ConstantConditions")
    void sendFeedback(int messageIndex, @NonNull UsedeskFeedback feedback) {
        Set<Integer> feedbacks = new HashSet<>(feedbacksLiveData.getValue().size() + 1);
        feedbacks.addAll(feedbacksLiveData.getValue());
        feedbacks.add(messageIndex);
        feedbacksLiveData.postValue(feedbacks);

        justComplete(usedeskChat.sendRx(feedback));
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (!disposables.isDisposed()) {
            disposables.dispose();
        }

        UsedeskChatSdk.release();
    }

    @SuppressWarnings("ConstantConditions")
    void detachFile(@NonNull UsedeskFileInfo usedeskFileInfo) {
        List<UsedeskFileInfo> attachedFileInfoList = new ArrayList<>(fileInfoListLiveData.getValue());
        attachedFileInfoList.remove(usedeskFileInfo);
        setAttachedFileInfoList(attachedFileInfoList);
    }

    void onSend(@NonNull String textMessage) {
        justComplete(usedeskChat.sendRx(textMessage));
        justComplete(usedeskChat.sendRx(fileInfoListLiveData.getValue()), throwable -> {
            largeFileSizeErrorLiveData.postValue(new UsedeskSingleLifeEvent<>());
        });

        clearFileInfoList();
    }

    void onSend(@NonNull String name, @NonNull String email, @NonNull String message) {
        justComplete(usedeskChat.sendRx(new UsedeskOfflineForm(name, email, message))
                .doOnComplete(() -> messagePanelStateLiveData.postValue(MessagePanelState.OFFLINE_FORM_SENT)));
    }

    void onNameChanged(@NonNull String name) {
        nameLiveData.setValue(name);
    }

    void onEmailChanged(@NonNull String email) {
        emailLiveData.setValue(email);
    }
}
