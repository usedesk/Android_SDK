package ru.usedesk.chat_sdk.domain

import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
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
    private var lastMessages = listOf<UsedeskMessage>()
    private var initClientMessage: String? = null
    private var actionListeners = mutableSetOf<IUsedeskActionListener>()
    private var reconnectDisposable: Disposable? = null

    private val eventListener = object : IApiRepository.EventListener {
        override fun onConnected() {
            actionListeners.forEach {
                it.onConnected()
            }
        }

        override fun onDisconnected() {
            if (reconnectDisposable?.isDisposed != false) {
                reconnectDisposable = Completable.timer(5, TimeUnit.SECONDS)
                        .subscribe {
                            try {
                                connect()
                            } catch (e: Exception) {
                                //nothing
                            }
                        }
            }

            actionListeners.forEach {
                it.onDisconnected()
            }
        }

        override fun onTokenError() {
            userInfoRepository.setToken(null)
            try {
                token?.also {
                    apiRepository.init(configuration, it)
                }
            } catch (e: UsedeskException) {
                actionListeners.forEach {
                    it.onException(e)
                }
            }
        }

        override fun onFeedback() {
            actionListeners.forEach {
                it.onFeedbackReceived()
            }
        }

        override fun onException(exception: Exception) {
            actionListeners.forEach {
                it.onException(exception)
            }
        }

        override fun onChatInited(chatInited: ChatInited) {
            this@ChatInteractor.onChatInited(chatInited)
        }

        override fun onMessagesReceived(newMessages: List<UsedeskMessage>) {
            this@ChatInteractor.onMessagesNew(newMessages)
        }

        override fun onMessageUpdated(message: UsedeskMessage) {
            this@ChatInteractor.onMessageUpdate(message)
        }

        override fun onOfflineForm() {
            actionListeners.forEach {
                it.onOfflineFormExpected(configuration)
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
                configuration.url,
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
        actionListeners.forEach {
            it.onMessageUpdated(message)
        }
    }

    private fun onMessagesNew(messages: List<UsedeskMessage>) {
        messages.forEach {
            lastMessages = lastMessages + it
            actionListeners.forEach { listener ->
                listener.onMessageReceived(it)
            }
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

        actionListeners.forEach {
            it.onConnected()
            it.onMessagesReceived(chatChatInited.messages)
        }

        val initClientMessage = try {
            userInfoRepository.getConfiguration().initClientMessage
        } catch (ignore: UsedeskException) {
            null
        }
        if (initClientMessage != null) {
            if (initClientMessage != configuration.initClientMessage) {
                send(initClientMessage)
            }
        }
        userInfoRepository.setConfiguration(configuration)

        if (chatChatInited.waitingEmail) {
            sendUserEmail()
        } else {
            eventListener.onSetEmailSuccess()
        }
    }
}