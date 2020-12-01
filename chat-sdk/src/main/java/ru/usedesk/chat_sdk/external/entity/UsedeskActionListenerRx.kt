package ru.usedesk.chat_sdk.external.entity

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class UsedeskActionListenerRx : IUsedeskActionListener {
    private val connectedSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val disconnectedSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val connectedStateSubject = BehaviorSubject.createDefault(false)

    private val chatItemSubject = BehaviorSubject.create<UsedeskChatItem>()
    private val newChatItemSubject = BehaviorSubject.create<UsedeskChatItem>()
    private val chatItemsSubject = BehaviorSubject.create<List<UsedeskChatItem>>()

    private val offlineFormExpectedSubject = BehaviorSubject.create<UsedeskChatConfiguration>()
    private val feedbackSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val exceptionSubject = BehaviorSubject.create<Exception>()

    private var lastChatItems = listOf<UsedeskChatItem>()

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
    val chatItemObservable: Observable<UsedeskChatItem> = chatItemSubject

    /**
     * Только новые сообщения, генерируемые после подписки
     */
    val newChatItemObservable: Observable<UsedeskChatItem> = newChatItemSubject

    /**
     * Список всех сообщений (обновляется с каждым новым сообщением)
     */
    val chatItemsObservable: Observable<List<UsedeskChatItem>> = chatItemsSubject

    val offlineFormExpectedObservable: Observable<UsedeskChatConfiguration> = offlineFormExpectedSubject

    val disconnectedObservable: Observable<UsedeskEvent<Any?>> = disconnectedSubject

    val exceptionObservable: Observable<Exception> = exceptionSubject

    val feedbackObservable: Observable<UsedeskEvent<Any?>> = feedbackSubject

    override fun onConnected() {
        connectedSubject.onNext(UsedeskSingleLifeEvent(null))
        connectedStateSubject.onNext(true)
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

    override fun onException(usedeskException: Exception) {
        exceptionSubject.onNext(usedeskException)
    }
}
