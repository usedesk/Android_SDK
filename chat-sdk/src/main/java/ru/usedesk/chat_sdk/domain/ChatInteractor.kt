package ru.usedesk.chat_sdk.domain

import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.entity.ChatInited
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import toothpick.InjectConstructor
import java.util.*
import java.util.concurrent.TimeUnit

@InjectConstructor
internal class ChatInteractor(
        private val configuration: UsedeskChatConfiguration,
        private val userInfoRepository: IUserInfoRepository,
        private val apiRepository: IApiRepository
) : IUsedeskChat {

    private var token: String? = null
    private var initClientMessage: String? = null

    private var actionListeners = mutableSetOf<IUsedeskActionListener>()
    private var actionListenersRx = mutableSetOf<IUsedeskActionListenerRx>()

    private val connectedSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val disconnectedSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val connectedStateSubject = BehaviorSubject.createDefault(false)
    private val messagesSubject = BehaviorSubject.create<List<UsedeskMessage>>()
    private val messageSubject = BehaviorSubject.create<UsedeskMessage>()
    private val newMessageSubject = PublishSubject.create<UsedeskMessage>()
    private val messageUpdateSubject = BehaviorSubject.create<UsedeskMessage>()
    private val offlineFormExpectedSubject = BehaviorSubject.create<UsedeskChatConfiguration>()
    private val feedbackSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val exceptionSubject = BehaviorSubject.create<Exception>()

    private var reconnectDisposable: Disposable? = null

    private var lastMessages = listOf<UsedeskMessage>()

    private val eventListener = object : IApiRepository.EventListener {
        override fun onConnected() {
            connectedSubject.onNext(UsedeskSingleLifeEvent(null))
            connectedStateSubject.onNext(true)
            actionListeners.forEach { listener ->
                listener.onConnected()
            }
        }

        override fun onDisconnected() {
            if (reconnectDisposable?.isDisposed != false) {
                reconnectDisposable = Completable.timer(5, TimeUnit.SECONDS).subscribe {
                    try {
                        connect()
                    } catch (e: Exception) {
                        //nothing
                    }
                }
            }

            disconnectedSubject.onNext(UsedeskSingleLifeEvent(null))
            connectedStateSubject.onNext(false)
            actionListeners.forEach { listener ->
                listener.onDisconnected()
            }
        }

        override fun onTokenError() {
            userInfoRepository.setToken(null)
            try {
                token?.also {
                    apiRepository.init(configuration, it)
                }
            } catch (e: UsedeskException) {
                onException(e)
            }
        }

        override fun onFeedback() {
            feedbackSubject.onNext(UsedeskSingleLifeEvent(null))
            actionListeners.forEach { listener ->
                listener.onFeedbackReceived()
            }
        }

        override fun onException(exception: Exception) {
            exceptionSubject.onNext(exception)
            actionListeners.forEach { listener ->
                listener.onException(exception)
            }
        }

        override fun onChatInited(chatInited: ChatInited) {
            this@ChatInteractor.onChatInited(chatInited)
        }

        override fun onMessagesReceived(newMessages: List<UsedeskMessage>) {
            this@ChatInteractor.onMessagesNew(newMessages, false)
        }

        override fun onMessageUpdated(message: UsedeskMessage) {
            this@ChatInteractor.onMessageUpdate(message)
        }

        override fun onOfflineForm() {
            offlineFormExpectedSubject.onNext(configuration)
            actionListeners.forEach { listener ->
                listener.onOfflineFormExpected(configuration)
            }
        }

        override fun onSetEmailSuccess() {
            initClientMessage?.also {
                if (it.isNotEmpty()) {
                    try {
                        send(it)
                        initClientMessage = ""
                    } catch (e: Exception) {
                        //nothing
                    }
                }
            }
        }
    }

    @Throws(UsedeskException::class)
    override fun connect() {
        try {
            reconnectDisposable?.dispose()
            reconnectDisposable = null
            val configuration = userInfoRepository.getConfiguration()
            if (this.configuration.email == configuration.email && this.configuration.companyId == configuration.companyId) {
                token = userInfoRepository.getToken()
            }
        } catch (e: UsedeskDataNotFoundException) {
            e.printStackTrace()
        }
        apiRepository.connect(
                configuration.urlChat,
                token,
                configuration,
                eventListener
        )
    }

    private fun onMessageUpdate(message: UsedeskMessage) {
        lastMessages = lastMessages.map {
            if (it.id == message.id) {
                message
            } else {
                it
            }
        }
        messageUpdateSubject.onNext(message)
        actionListeners.forEach { listener ->
            listener.onMessageUpdated(message)
        }
    }

    private fun onMessagesNew(messages: List<UsedeskMessage>,
                              isInited: Boolean) {
        lastMessages = lastMessages + messages
        messages.forEach {
            messageSubject.onNext(it)
            if (!isInited) {
                newMessageSubject.onNext(it)
            }
            actionListeners.forEach { listener ->
                listener.onMessageReceived(it)
            }
        }
        messagesSubject.onNext(lastMessages)
        actionListeners.forEach { listener ->
            listener.onMessagesReceived(lastMessages)
        }
    }

    override fun disconnect() {
        apiRepository.disconnect()
    }

    override fun addActionListener(listener: IUsedeskActionListener) {
        actionListeners.add(listener)
    }

    override fun removeActionListener(listener: IUsedeskActionListener) {
        actionListeners.remove(listener)
    }

    override fun addActionListener(listener: IUsedeskActionListenerRx) {
        actionListenersRx.add(listener)
        listener.onObservables(
                connectedSubject,
                disconnectedSubject,
                connectedStateSubject,
                messageSubject,
                newMessageSubject,
                messagesSubject,
                messageUpdateSubject,
                offlineFormExpectedSubject,
                feedbackSubject,
                exceptionSubject
        )
    }

    override fun removeActionListener(listener: IUsedeskActionListenerRx) {
        actionListenersRx.remove(listener)
        listener.onDispose()
    }

    override fun isNoSubscribers(): Boolean {
        return actionListeners.isEmpty() && actionListenersRx.isEmpty()
    }

    @Throws(UsedeskException::class)
    override fun send(textMessage: String) {
        if (textMessage.isNotEmpty()) {
            token?.also {
                apiRepository.send(it, textMessage)
            }
        }
    }

    @Throws(UsedeskException::class)
    override fun send(usedeskFileInfo: UsedeskFileInfo) {
        token?.also {
            apiRepository.send(configuration, it, usedeskFileInfo)
        }
    }

    @Throws(UsedeskException::class)
    override fun send(usedeskFileInfoList: List<UsedeskFileInfo>) {
        for (usedeskFileInfo in usedeskFileInfoList) {
            send(usedeskFileInfo)
        }
    }

    @Throws(UsedeskException::class)
    override fun send(message: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        token?.also {
            apiRepository.send(it, feedback)

            onMessageUpdate(UsedeskMessageAgentText(
                    message.id,
                    Calendar.getInstance(),
                    message.text,
                    message.buttons,
                    false,
                    feedback,
                    message.name,
                    message.avatar
            ))
        }
    }

    @Throws(UsedeskException::class)
    override fun send(offlineForm: UsedeskOfflineForm) {
        apiRepository.send(configuration, configuration.companyId, offlineForm)
    }

    override fun connectRx(): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            connect()
            emitter.onComplete()
        }
    }

    override fun sendRx(textMessage: String): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            send(textMessage)
            emitter.onComplete()
        }
    }

    override fun sendRx(usedeskFileInfo: UsedeskFileInfo): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            send(usedeskFileInfo)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun sendRx(usedeskFileInfoList: List<UsedeskFileInfo>): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            send(usedeskFileInfoList)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun sendRx(message: UsedeskMessageAgentText, feedback: UsedeskFeedback): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            send(message, feedback)
            emitter.onComplete()
        }
    }

    override fun sendRx(offlineForm: UsedeskOfflineForm): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            send(offlineForm)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun disconnectRx(): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            disconnect()
            emitter.onComplete()
        }
    }

    private fun sendUserEmail() {
        try {
            token?.also {
                apiRepository.send(it,
                        configuration.email,
                        configuration.clientName,
                        configuration.clientPhoneNumber,
                        configuration.clientAdditionalId)
            }
        } catch (e: UsedeskException) {
            actionListeners.forEach {
                it.onException(e)
            }
        }
    }

    private fun onChatInited(chatChatInited: ChatInited) {
        this.token = chatChatInited.token

        userInfoRepository.setToken(token)

        onMessagesNew(chatChatInited.messages, true)

        val initClientMessage = try {
            userInfoRepository.getConfiguration().clientInitMessage
        } catch (ignore: UsedeskException) {
            null
        }
        if (initClientMessage?.equals(configuration.clientInitMessage) == false) {
            send(initClientMessage)
        }
        userInfoRepository.setConfiguration(configuration)

        if (chatChatInited.waitingEmail) {
            sendUserEmail()
        } else {
            eventListener.onSetEmailSuccess()
        }
    }
}