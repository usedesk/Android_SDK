package ru.usedesk.chat_sdk.external.entity

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import ru.usedesk.chat_sdk.external.entity.chat.*
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import java.util.*

class UsedeskActionListenerRx : IUsedeskActionListener {
    private val connectedSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val disconnectedSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val connectedStateSubject = BehaviorSubject.createDefault(false)

    private val messageSubject = BehaviorSubject.create<UsedeskMessage>()
    private val newMessageSubject = BehaviorSubject.create<UsedeskMessage>()
    private val messagesSubject = BehaviorSubject.create<List<UsedeskMessage>>()

    private val chatItemSubject = BehaviorSubject.create<UsedeskChatItem>()
    private val newChatItemSubject = BehaviorSubject.create<UsedeskChatItem>()
    private val chatItemsSubject = BehaviorSubject.create<List<UsedeskChatItem>>()

    private val offlineFormExpectedSubject = BehaviorSubject.create<UsedeskChatConfiguration>()
    private val feedbackSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val exceptionSubject = BehaviorSubject.create<UsedeskException>()

    private var lastMessages = listOf<UsedeskMessage>()
    private var lastChatItems = listOf<UsedeskChatItem>()

    private fun onNewMessages(newMessages: List<UsedeskMessage>) {
        postMessages(lastMessages + newMessages)
    }

    private fun postMessages(messages: List<UsedeskMessage>) {
        lastMessages = messages
        messagesSubject.onNext(messages)
    }

    private fun onNewChatItems(newChatItems: List<UsedeskChatItem>) {
        postChatItems(lastChatItems + newChatItems)
    }

    private fun postChatItems(chatItems: List<UsedeskChatItem>) {
        lastChatItems = chatItems
        chatItemsSubject.onNext(chatItems)
    }

    val connectedStateObservable: Observable<Boolean> = connectedStateSubject

    val connectedObservable: Observable<UsedeskEvent<Any?>> = connectedSubject

    /**
     * Каждое сообщение по отдельности
     */
    val messageObservable: Observable<UsedeskMessage> = messageSubject
    val chatItemObservable: Observable<UsedeskChatItem> = chatItemSubject

    /**
     * Только новые сообщения, генерируемые после подписки
     */
    val newMessageObservable: Observable<UsedeskMessage> = newMessageSubject
    val newChatItemObservable: Observable<UsedeskChatItem> = newChatItemSubject

    /**
     * Список всех сообщений (обновляется с каждым новым сообщением)
     */
    val messagesObservable: Observable<List<UsedeskMessage>> = messagesSubject
    val chatItemsObservable: Observable<List<UsedeskChatItem>> = chatItemsSubject

    val offlineFormExpectedObservable: Observable<UsedeskChatConfiguration> = offlineFormExpectedSubject

    val disconnectedObservable: Observable<UsedeskEvent<Any?>> = disconnectedSubject

    val exceptionObservable: Observable<UsedeskException> = exceptionSubject

    val feedbackObservable: Observable<UsedeskEvent<Any?>> = feedbackSubject

    private fun convert(usedeskMessage: UsedeskMessage): UsedeskChatItem? {//TODO: перенести это в interactor
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
                    UsedeskMessageClientImage(messageDate,
                            usedeskMessage.file)
                } else {
                    UsedeskMessageAgentImage(messageDate,
                            usedeskMessage.file,
                            usedeskMessage.name ?: "",
                            usedeskMessage.usedeskPayload?.avatar ?: "")
                }
            } else {
                if (fromClient) {
                    UsedeskMessageClientFile(messageDate,
                            usedeskMessage.file)
                } else {
                    UsedeskMessageAgentFile(messageDate,
                            usedeskMessage.file,
                            usedeskMessage.name ?: "",
                            usedeskMessage.usedeskPayload?.avatar ?: "")
                }
            }
        } else {
            val text: String
            val html: String

            val divIndex = usedeskMessage.text.indexOf("<div")

            if (divIndex >= 0) {
                text = usedeskMessage.text.substring(0, divIndex)

                html = usedeskMessage.text.removePrefix(text)
            } else {
                text = usedeskMessage.text
                html = ""
            }

            val convertedText = text
                    .replace("<strong data-verified=\"redactor\" data-redactor-tag=\"strong\">", "<b>")
                    .replace("</strong>", "</b>")
                    .replace("<em data-verified=\"redactor\" data-redactor-tag=\"em\">", "<i>")
                    .replace("</em>", "</i>")
                    .replace("</p>", "")
                    .removePrefix("<p>")
                    .trim()

            if (text.isEmpty() && html.isEmpty()) {
                null
            } else if (fromClient) {
                UsedeskMessageClientText(messageDate,
                        convertedText,
                        html)
            } else {
                UsedeskMessageAgentText(messageDate,
                        convertedText,
                        html,
                        usedeskMessage.name ?: "",
                        usedeskMessage.usedeskPayload?.avatar ?: "")
            }
        }
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

    override fun onChatItemReceived(chatItem: UsedeskChatItem) {
        chatItemSubject.onNext(chatItem)
        newChatItemSubject.onNext(chatItem)
        onNewChatItems(listOf(chatItem))
    }

    override fun onChatItemsReceived(chatItems: List<UsedeskChatItem>) {
        chatItems.forEach {
            chatItemSubject.onNext(it)
        }
        postChatItems(chatItems)
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

    override fun onException(usedeskException: UsedeskException) {
        exceptionSubject.onNext(usedeskException)
    }
}
