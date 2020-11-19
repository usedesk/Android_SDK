package ru.usedesk.chat_sdk.external.entity

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import ru.usedesk.chat_sdk.external.entity.ticketitem.*
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import java.util.*
import javax.inject.Inject

class UsedeskActionListenerRx @Inject constructor() : IUsedeskActionListener {
    private val connectedSubject = BehaviorSubject.create<UsedeskSingleLifeEvent<*>>()
    private val disconnectedSubject = BehaviorSubject.create<UsedeskSingleLifeEvent<*>>()
    private val connectedStateSubject = BehaviorSubject.createDefault(false)
    private val messageSubject = BehaviorSubject.create<UsedeskMessage>()
    private val newMessageSubject = BehaviorSubject.create<UsedeskMessage>()
    private val messagesSubject = BehaviorSubject.create<List<UsedeskMessage>>()
    private val offlineFormExpectedSubject = BehaviorSubject.create<UsedeskChatConfiguration>()
    private val feedbackSubject = BehaviorSubject.create<UsedeskSingleLifeEvent<*>>()
    private val exceptionSubject = BehaviorSubject.create<UsedeskException>()
    private var lastMessages = listOf<UsedeskMessage>()

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
     * Список всех сообщений (обновляется с каждым новым сообщением)
     */
    val messagesObservable: Observable<List<UsedeskMessage>>
        get() = messagesSubject

    /**
     * Список всех сообщений в виде TicketItem (обновляется с каждым новым сообщением)
     */
    fun getTicketItemsObservable(): Observable<List<ChatItem>> {
        return messagesSubject.map { messages ->
            messages.mapNotNull { message ->
                convert(message)
            }
        }
    }

    private fun convert(usedeskMessage: UsedeskMessage): ChatItem? {
        val fromClient: Boolean = when (usedeskMessage.type) {
            UsedeskMessageType.CLIENT_TO_OPERATOR,
            UsedeskMessageType.CLIENT_TO_BOT -> {
                true
            }
            UsedeskMessageType.OPERATOR_TO_CLIENT,
            UsedeskMessageType.BOT_TO_CLIENT -> {
                false
            }
            else -> {
                return null
            }
        }
        val messageDate = Calendar.getInstance()
        return if (usedeskMessage.file != null) {
            if (usedeskMessage.file.isImage()) {
                if (fromClient) {
                    MessageClientImage(messageDate,
                            usedeskMessage.file,
                            true)
                } else {
                    MessageAgentImage(messageDate,
                            usedeskMessage.file,
                            usedeskMessage.name,
                            usedeskMessage.usedeskPayload.avatar)
                }
            } else {
                if (fromClient) {
                    MessageClientFile(messageDate,
                            usedeskMessage.file,
                            true)
                } else {
                    MessageAgentFile(messageDate,
                            usedeskMessage.file,
                            usedeskMessage.name,
                            usedeskMessage.usedeskPayload.avatar)
                }
            }
        } else {
            if (fromClient) {
                MessageClientText(messageDate,
                        usedeskMessage.text,
                        true)
            } else {
                MessageAgentText(messageDate,
                        usedeskMessage.text,
                        usedeskMessage.name,
                        usedeskMessage.usedeskPayload.avatar)
            }
        }
    }

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