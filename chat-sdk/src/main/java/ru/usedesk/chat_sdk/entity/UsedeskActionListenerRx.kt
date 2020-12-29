package ru.usedesk.chat_sdk.entity

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class UsedeskActionListenerRx : IUsedeskActionListener {

    private val connectedSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    val connectedObservable: Observable<UsedeskEvent<Any?>> = connectedSubject

    private val disconnectedSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    val disconnectedObservable: Observable<UsedeskEvent<Any?>> = disconnectedSubject

    private val connectedStateSubject = BehaviorSubject.createDefault(false)
    val connectedStateObservable: Observable<Boolean> = connectedStateSubject

    private val messageSubject = BehaviorSubject.create<UsedeskMessage>()
    val messageObservable: Observable<UsedeskMessage> = messageSubject//Каждое сообщение по отдельности

    private val newMessageSubject = BehaviorSubject.create<UsedeskMessage>()
    val newMessageObservable: Observable<UsedeskMessage> = newMessageSubject//Только новые сообщения, генерируемые после подписки

    private val messagesSubject = BehaviorSubject.create<List<UsedeskMessage>>()
    val messagesObservable: Observable<List<UsedeskMessage>> = messagesSubject//Список всех сообщений (обновляется с каждым новым сообщением)

    private val messageUpdateSubject = BehaviorSubject.create<UsedeskMessage>()
    val messageUpdateObservable: Observable<UsedeskMessage> = messageUpdateSubject//Сообщение, изменившее своё состояние

    private val offlineFormExpectedSubject = BehaviorSubject.create<UsedeskChatConfiguration>()
    val offlineFormExpectedObservable: Observable<UsedeskChatConfiguration> = offlineFormExpectedSubject

    private val feedbackSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    val feedbackObservable: Observable<UsedeskEvent<Any?>> = feedbackSubject

    private val exceptionSubject = BehaviorSubject.create<Exception>()
    val exceptionObservable: Observable<Exception> = exceptionSubject

    private var lastMessages = listOf<UsedeskMessage>()

    private fun onNewMessages(newMessages: List<UsedeskMessage>) {
        postMessages(lastMessages + newMessages)
    }

    private fun postMessages(messages: List<UsedeskMessage>) {
        lastMessages = messages
        messagesSubject.onNext(messages)
    }

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

    override fun onMessageUpdated(message: UsedeskMessage) {
        messageUpdateSubject.onNext(message)
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
