package ru.usedesk.chat_sdk.entity

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

abstract class IUsedeskActionListenerRx {

    private lateinit var disposables: List<Disposable?>

    fun onObservables(
            connectedObservable: Observable<UsedeskEvent<Any?>>,
            disconnectedObservable: Observable<UsedeskEvent<Any?>>,
            connectedStateObservable: Observable<Boolean>,
            messageObservable: Observable<UsedeskMessage>,
            newMessageObservable: Observable<UsedeskMessage>,
            messagesObservable: Observable<List<UsedeskMessage>>,
            messageUpdateObservable: Observable<UsedeskMessage>,
            offlineFormExpectedObservable: Observable<UsedeskChatConfiguration>,
            feedbackObservable: Observable<UsedeskEvent<Any?>>,
            exceptionObservable: Observable<Exception>
    ) {
        disposables = listOf(
                onConnectedObservable(connectedObservable),
                onDisconnectedObservable(disconnectedObservable),
                onConnectedStateObservable(connectedStateObservable),
                onMessageObservable(messageObservable),
                onNewMessageObservable(newMessageObservable),
                onMessagesObservable(messagesObservable),
                onMessageUpdateObservable(messageUpdateObservable),
                onOfflineFormExpectedObservable(offlineFormExpectedObservable),
                onFeedbackObservable(feedbackObservable),
                onExceptionObservable(exceptionObservable)
        )
    }

    open fun onConnectedObservable(connectedObservable: Observable<UsedeskEvent<Any?>>): Disposable? = null

    open fun onDisconnectedObservable(disconnectedObservable: Observable<UsedeskEvent<Any?>>): Disposable? = null

    open fun onConnectedStateObservable(connectedStateObservable: Observable<Boolean>): Disposable? = null

    open fun onMessageObservable(messageObservable: Observable<UsedeskMessage>): Disposable? = null

    open fun onNewMessageObservable(newMessageObservable: Observable<UsedeskMessage>): Disposable? = null

    open fun onMessagesObservable(messagesObservable: Observable<List<UsedeskMessage>>): Disposable? = null

    open fun onMessageUpdateObservable(messageUpdateObservable: Observable<UsedeskMessage>): Disposable? = null

    open fun onOfflineFormExpectedObservable(offlineFormExpectedObservable: Observable<UsedeskChatConfiguration>): Disposable? = null

    open fun onFeedbackObservable(feedbackObservable: Observable<UsedeskEvent<Any?>>): Disposable? = null

    open fun onExceptionObservable(exceptionObservable: Observable<Exception>): Disposable? = null

    open fun onDispose() {
        disposables.forEach {
            it?.dispose()
        }
    }
}