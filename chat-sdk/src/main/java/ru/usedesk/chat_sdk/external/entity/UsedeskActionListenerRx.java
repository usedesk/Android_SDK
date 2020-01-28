package ru.usedesk.chat_sdk.external.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public class UsedeskActionListenerRx implements IUsedeskActionListener {

    private final Subject<UsedeskSingleLifeEvent> connectedSubject = BehaviorSubject.create();
    private final Subject<UsedeskSingleLifeEvent> disconnectedSubject = BehaviorSubject.create();
    private final Subject<Boolean> connectedStateSubject = BehaviorSubject.createDefault(false);

    private final Subject<UsedeskMessage> messageSubject = BehaviorSubject.create();
    private final Subject<UsedeskMessage> newMessageSubject = BehaviorSubject.create();
    private final Subject<List<UsedeskMessage>> messagesSubject = BehaviorSubject.create();

    private final Subject<UsedeskChatConfiguration> offlineFormExpectedSubject = BehaviorSubject.create();
    private final Subject<UsedeskSingleLifeEvent> feedbackSubject = BehaviorSubject.create();
    private final Subject<UsedeskException> exceptionSubject = BehaviorSubject.create();

    private List<UsedeskMessage> lastMessages = new ArrayList<>();

    @Inject
    public UsedeskActionListenerRx() {
    }

    private void onNewMessages(@NonNull List<UsedeskMessage> newMessages) {
        List<UsedeskMessage> messages = new ArrayList<>(lastMessages.size() + newMessages.size());
        messages.addAll(lastMessages);
        messages.addAll(newMessages);
        lastMessages = messages;

        messagesSubject.onNext(messages);
    }

    @NonNull
    public Observable<Boolean> getConnectedStateSubject() {
        return connectedStateSubject;
    }

    @NonNull
    public Observable<UsedeskSingleLifeEvent> getConnectedObservable() {
        return connectedSubject;
    }

    /**
     * Каждое сообщение
     */
    @NonNull
    public Observable<UsedeskMessage> getMessageObservable() {
        return messageSubject;
    }

    /**
     * Только новые сообщения, генерируемые после подписки
     */
    @NonNull
    public Observable<UsedeskMessage> getNewMessageObservable() {
        return newMessageSubject;
    }

    public Observable<List<UsedeskMessage>> getMessagesObservable() {
        return messagesSubject;
    }

    @NonNull
    public Observable<UsedeskChatConfiguration> getOfflineFormExpectedObservable() {
        return offlineFormExpectedSubject;
    }

    @NonNull
    public Observable<UsedeskSingleLifeEvent> getDisconnectedObservable() {
        return disconnectedSubject;
    }

    @NonNull
    public Observable<UsedeskException> getExceptionObservable() {
        return exceptionSubject;
    }

    @NonNull
    public Observable<UsedeskSingleLifeEvent> getFeedbackObservable() {
        return feedbackSubject;
    }

    @Override
    public void onConnected() {
        connectedSubject.onNext(new UsedeskSingleLifeEvent());
        connectedStateSubject.onNext(true);
    }

    @Override
    public void onMessageReceived(@NonNull UsedeskMessage message) {
        messageSubject.onNext(message);
        newMessageSubject.onNext(message);
        onNewMessages(Collections.singletonList(message));
    }

    @Override
    public void onMessagesReceived(@NonNull List<UsedeskMessage> messages) {
        for (UsedeskMessage message : messages) {
            messageSubject.onNext(message);
        }

        onNewMessages(messages);
    }

    @Override
    public void onFeedbackReceived() {
        feedbackSubject.onNext(new UsedeskSingleLifeEvent());
    }

    @Override
    public void onOfflineFormExpected(@NonNull UsedeskChatConfiguration chatConfiguration) {
        offlineFormExpectedSubject.onNext(chatConfiguration);
    }

    @Override
    public void onDisconnected() {
        connectedSubject.onNext(new UsedeskSingleLifeEvent());
        connectedStateSubject.onNext(false);
    }

    @Override
    public void onException(@NonNull UsedeskException usedeskException) {
        exceptionSubject.onNext(usedeskException);
    }
}
