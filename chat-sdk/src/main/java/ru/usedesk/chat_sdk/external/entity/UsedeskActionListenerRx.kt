package ru.usedesk.chat_sdk.external.entity

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import java.util.*
import javax.inject.Inject

class UsedeskActionListenerRx @Inject constructor() : IUsedeskActionListener {
    private val connectedSubject: Subject<UsedeskSingleLifeEvent<*>> = BehaviorSubject.create()
    private val disconnectedSubject: Subject<UsedeskSingleLifeEvent<*>> = BehaviorSubject.create()
    private val connectedStateSubject: Subject<Boolean> = BehaviorSubject.createDefault(false)
    private val messageSubject: Subject<UsedeskMessage> = BehaviorSubject.create()
    private val newMessageSubject: Subject<UsedeskMessage> = BehaviorSubject.create()
    private val messagesSubject: Subject<List<UsedeskMessage>> = BehaviorSubject.create()
    private val offlineFormExpectedSubject: Subject<UsedeskChatConfiguration> = BehaviorSubject.create()
    private val feedbackSubject: Subject<UsedeskSingleLifeEvent<*>> = BehaviorSubject.create()
    private val exceptionSubject: Subject<UsedeskException> = BehaviorSubject.create()
    private var lastMessages: List<UsedeskMessage> = ArrayList()
    private fun onNewMessages(newMessages: List<UsedeskMessage>) {
        val messages: MutableList<UsedeskMessage> = ArrayList(lastMessages.size + newMessages.size)
        messages.addAll(lastMessages)
        messages.addAll(newMessages)
        postMessages(messages)
    }

    private fun postMessages(messages: List<UsedeskMessage>) {
        lastMessages = messages
        messagesSubject.onNext(messages)
    }

    fun getConnectedStateSubject(): Observable<Boolean> {
        return connectedStateSubject
    }

    val connectedObservable: Observable<UsedeskSingleLifeEvent<*>>
        get() = connectedSubject

    /**
     * Каждое сообщение
     */
    val messageObservable: Observable<UsedeskMessage>
        get() = messageSubject

    /**
     * Только новые сообщения, генерируемые после подписки
     */
    val newMessageObservable: Observable<UsedeskMessage>
        get() = newMessageSubject

    /**
     * Список всех сообщений (обновляется с каждым новым)
     */
    val messagesObservable: Observable<List<UsedeskMessage>>
        get() = messagesSubject
    val offlineFormExpectedObservable: Observable<UsedeskChatConfiguration>
        get() = offlineFormExpectedSubject
    val disconnectedObservable: Observable<UsedeskSingleLifeEvent<*>>
        get() = disconnectedSubject
    val exceptionObservable: Observable<UsedeskException>
        get() = exceptionSubject
    val feedbackObservable: Observable<UsedeskSingleLifeEvent<*>>
        get() = feedbackSubject

    override fun onConnected() {
        connectedSubject.onNext(UsedeskSingleLifeEvent<Any?>())
        connectedStateSubject.onNext(true)
    }

    override fun onMessageReceived(message: UsedeskMessage) {
        messageSubject.onNext(message)
        newMessageSubject.onNext(message)
        onNewMessages(listOf(message))
    }

    override fun onMessagesReceived(messages: List<UsedeskMessage>) {
        for (message in messages) {
            messageSubject.onNext(message)
        }
        postMessages(messages)
    }

    override fun onFeedbackReceived() {
        feedbackSubject.onNext(UsedeskSingleLifeEvent<Any?>())
    }

    override fun onOfflineFormExpected(chatConfiguration: UsedeskChatConfiguration) {
        offlineFormExpectedSubject.onNext(chatConfiguration)
    }

    override fun onDisconnected() {
        connectedSubject.onNext(UsedeskSingleLifeEvent<Any?>())
        connectedStateSubject.onNext(false)
    }

    override fun onException(usedeskException: UsedeskException) {
        exceptionSubject.onNext(usedeskException)
    }
}