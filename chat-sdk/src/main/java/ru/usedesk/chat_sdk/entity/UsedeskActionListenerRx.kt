package ru.usedesk.chat_sdk.entity

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class UsedeskActionListenerRx : IUsedeskActionListener {
    private val connectedSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val disconnectedSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val connectedStateSubject = BehaviorSubject.createDefault(false)

    private val messageSubject = BehaviorSubject.create<UsedeskMessage>()
    private val newMessageSubject = BehaviorSubject.create<UsedeskMessage>()
    private val messagesSubject = BehaviorSubject.create<List<UsedeskMessage>>()

    private val offlineFormExpectedSubject = BehaviorSubject.create<UsedeskChatConfiguration>()
    private val feedbackSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val exceptionSubject = BehaviorSubject.create<Exception>()

    private var lastMessages = listOf<UsedeskMessage>()

    private fun onNewMessages(newMessages: List<UsedeskMessage>) {
        postMessages(lastMessages + newMessages)
    }

    private fun postMessages(messages: List<UsedeskMessage>) {
        lastMessages = messages
        messagesSubject.onNext(messages)
    }

    val connectedStateObservable: Observable<Boolean> = connectedStateSubject

    val connectedObservable: Observable<UsedeskEvent<Any?>> = connectedSubject

    /**
     * Каждое сообщение по отдельности
     */
    val messageObservable: Observable<UsedeskMessage> = messageSubject

    /**
     * Только новые сообщения, генерируемые после подписки
     */
    val newMessageObservable: Observable<UsedeskMessage> = newMessageSubject

    /**
     * Список всех сообщений (обновляется с каждым новым сообщением)
     */
    val messagesObservable: Observable<List<UsedeskMessage>> = messagesSubject

    val offlineFormExpectedObservable: Observable<UsedeskChatConfiguration> = offlineFormExpectedSubject

    val disconnectedObservable: Observable<UsedeskEvent<Any?>> = disconnectedSubject

    val exceptionObservable: Observable<Exception> = exceptionSubject

    val feedbackObservable: Observable<UsedeskEvent<Any?>> = feedbackSubject

    override fun onConnected() {
        connectedSubject.onNext(UsedeskSingleLifeEvent(null))
        connectedStateSubject.onNext(true)
    }

    override fun onMessageReceived(message: UsedeskMessage) {
        messageSubject.onNext(message)
        newMessageSubject.onNext(message)
        onNewMessages(listOf(message))
    }

    override fun onMessagesReceived(messages: List<UsedeskMessage>) {
        messages.forEach {
            messageSubject.onNext(it)
        }
        postMessages(messages)
    }

    override fun onFeedbackReceived() {
        feedbackSubject.onNext(UsedeskSingleLifeEvent(null))
    }

    override fun onOfflineFormExpected(chatConfiguration: UsedeskChatConfiguration) {
        offlineFormExpectedSubject.onNext(chatConfiguration)
    }

    override fun onDisconnected() {
        connectedSubject.onNext(UsedeskSingleLifeEvent(null))
        connectedStateSubject.onNext(false)
    }

    override fun onException(usedeskException: Exception) {
        exceptionSubject.onNext(usedeskException)
    }
}
