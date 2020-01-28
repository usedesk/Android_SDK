package ru.usedesk.chat_sdk.external.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;

public class UsedeskActionListenerRx implements IUsedeskActionListener {

    private final Subject<UsedeskSingleLifeEvent> connectedSubject = BehaviorSubject.create();
    private final Subject<UsedeskChatConfiguration> offlineFormExpectedSubject = BehaviorSubject.create();
    private final Subject<UsedeskSingleLifeEvent> disconnectedSubject = BehaviorSubject.create();
    private final Subject<UsedeskSingleLifeEvent> feedbackSubject = BehaviorSubject.create();

    private final Subject<UsedeskMessage> messageSubject = BehaviorSubject.create();
    private final Subject<UsedeskMessage> newMessageSubject = BehaviorSubject.create();
    private final Subject<UsedeskException> exceptionSubject = BehaviorSubject.create();

    private final Observable<List<UsedeskMessage>> messagesObservable;

    @Inject
    public UsedeskActionListenerRx() {
        messagesObservable = messageSubject.scan(new ArrayList<>(), (messages, message) -> {
            List<UsedeskMessage> newMessages = new ArrayList<>(messages.size() + 1);
            newMessages.addAll(messages);
            newMessages.add(message);
            return newMessages;
        });
    }

    private void onMessage(UsedeskMessage message) {
        if (message != null) {
            messageSubject.onNext(message);
        }
    }

    private void onNewMessage(UsedeskMessage message) {
        if (message != null) {
            newMessageSubject.onNext(message);
        }
    }

    @NonNull
    public Observable<UsedeskSingleLifeEvent> getConnectedObservable() {
        return connectedSubject;
    }

    /**
     * Все сообщения
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

    /**
     * Полный список сообщений, генерируется с каждым сообщением
     */
    @NonNull
    public Observable<List<UsedeskMessage>> getMessagesObservable() {
        return messagesObservable;
    }

    @NonNull
    public Observable<UsedeskSingleLifeEvent> getOfflineFormExpectedObservable() {
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
    }

    @Override
    public void onMessageReceived(UsedeskMessage message) {
        onMessage(message);
        onNewMessage(message);
    }

    @Override
    public void onMessagesReceived(List<UsedeskMessage> messages) {
        if (messages != null) {
            for (UsedeskMessage message : messages) {
                onMessage(message);
            }
        }
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
    }

    @Override
    public void onException(@NonNull UsedeskException usedeskException) {
        exceptionSubject.onNext(usedeskException);
    }
}
