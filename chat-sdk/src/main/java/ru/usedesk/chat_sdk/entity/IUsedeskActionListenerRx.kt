package ru.usedesk.chat_sdk.entity

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.usedesk.common_sdk.entity.UsedeskEvent

abstract class IUsedeskActionListenerRx {

    private val disposables = CompositeDisposable()

    fun onObservables(
            connectedStateObservable: Observable<Boolean>,
            messageObservable: Observable<UsedeskMessage>,
            newMessageObservable: Observable<UsedeskMessage>,
            messagesObservable: Observable<List<UsedeskMessage>>,
            messageUpdateObservable: Observable<UsedeskMessage>,
            offlineFormExpectedObservable: Observable<UsedeskOfflineFormSettings>,
            feedbackObservable: Observable<UsedeskEvent<Any?>>,
            exceptionObservable: Observable<Exception>
    ) {
        listOfNotNull(onConnectedStateObservable(connectedStateObservable),
                onMessageObservable(messageObservable),
                onNewMessageObservable(newMessageObservable),
                onMessagesObservable(messagesObservable),
                onMessageUpdateObservable(messageUpdateObservable),
                onOfflineFormExpectedObservable(offlineFormExpectedObservable),
                onFeedbackObservable(feedbackObservable),
                onExceptionObservable(exceptionObservable)
        ).forEach {
            disposables.add(it)
        }
    }

    open fun onConnectedStateObservable(connectedStateObservable: Observable<Boolean>): Disposable? = null

    open fun onMessageObservable(messageObservable: Observable<UsedeskMessage>): Disposable? = null

    open fun onNewMessageObservable(newMessageObservable: Observable<UsedeskMessage>): Disposable? = null

    open fun onMessagesObservable(messagesObservable: Observable<List<UsedeskMessage>>): Disposable? = null

    open fun onMessageUpdateObservable(messageUpdateObservable: Observable<UsedeskMessage>): Disposable? = null

    open fun onOfflineFormExpectedObservable(offlineFormExpectedObservable: Observable<UsedeskOfflineFormSettings>): Disposable? = null

    open fun onFeedbackObservable(feedbackObservable: Observable<UsedeskEvent<Any?>>): Disposable? = null

    open fun onExceptionObservable(exceptionObservable: Observable<Exception>): Disposable? = null

    open fun onDispose() {
        disposables.dispose()
    }
}