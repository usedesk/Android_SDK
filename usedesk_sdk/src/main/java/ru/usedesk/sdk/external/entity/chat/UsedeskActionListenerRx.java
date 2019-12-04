package ru.usedesk.sdk.external.entity.chat;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import ru.usedesk.sdk.external.entity.exceptions.UsedeskException;

public class UsedeskActionListenerRx implements UsedeskActionListener {

    private final Subject<EmptyItem> connectedSubject = BehaviorSubject.create();
    private final Subject<Message> messageSubject = BehaviorSubject.create();
    private final Subject<Message> newMessageSubject = BehaviorSubject.create();
    private final Subject<EmptyItem> offlineFormExpectedSubject = BehaviorSubject.create();
    private final Subject<EmptyItem> disconnectedSubject = BehaviorSubject.create();
    private final Subject<Integer> errorResIdSubject = BehaviorSubject.create();
    private final Subject<Exception> errorSubject = BehaviorSubject.create();
    private final Subject<UsedeskException> exceptionSubject = BehaviorSubject.create();

    private final Observable<List<Message>> messagesObservable;

    @Inject
    public UsedeskActionListenerRx() {
        messagesObservable = messageSubject.scan(new ArrayList<>(), (messages, message) -> {
            List<Message> newMessages = new ArrayList<>(messages);
            newMessages.add(message);
            return newMessages;
        });
    }

    private void onMessage(Message message) {
        if (message != null) {
            messageSubject.onNext(message);
        }
    }

    private void onNewMessage(Message message) {
        if (message != null) {
            newMessageSubject.onNext(message);
        }
    }

    public Observable<EmptyItem> getConnectedObservable() {
        return connectedSubject;
    }

    /**
     * Все сообщения
     */
    public Observable<Message> getMessageObservable() {
        return messageSubject;
    }

    /**
     * Только новые сообщения, генерируемые после подписки
     */
    public Observable<Message> getNewMessageObservable() {
        return newMessageSubject;
    }

    /**
     * Полный список сообщений, генерируется с каждым сообщением
     */
    public Observable<List<Message>> getMessagesObservable() {
        return messagesObservable;
    }

    public Observable<EmptyItem> getOfflineFormExpectedObservable() {
        return offlineFormExpectedSubject;
    }

    public Observable<EmptyItem> getDisconnectedSubject() {
        return disconnectedSubject;
    }

    public Observable<Integer> getErrorResIdSubject() {
        return errorResIdSubject;
    }

    public Observable<Exception> getErrorSubject() {
        return errorSubject;
    }

    @Override
    public void onConnected() {
        connectedSubject.onNext(EmptyItem.IGNORE_ME);
    }

    @Override
    public void onMessageReceived(Message message) {
        onMessage(message);
        onNewMessage(message);
    }

    @Override
    public void onMessagesReceived(List<Message> messages) {
        if (messages != null) {
            for (Message message : messages) {
                onMessage(message);
            }
        }
    }

    @Override
    public void onServiceMessageReceived(Message message) {
        onMessage(message);
    }

    @Override
    public void onOfflineFormExpected() {
        connectedSubject.onNext(EmptyItem.IGNORE_ME);
    }

    @Override
    public void onDisconnected() {
        connectedSubject.onNext(EmptyItem.IGNORE_ME);
    }

    @Override
    @Deprecated
    public void onError(int errorResId) {
        errorResIdSubject.onNext(errorResId);
    }

    @Override
    @Deprecated
    public void onError(Exception e) {
        if (e != null) {
            errorSubject.onError(e);
        }
    }

    @Override
    public void onException(@NonNull UsedeskException usedeskException) {
        exceptionSubject.onNext(usedeskException);
    }

    public Subject<UsedeskException> getExceptionSubject() {
        return exceptionSubject;
    }

    public enum EmptyItem {
        IGNORE_ME
    }
}
