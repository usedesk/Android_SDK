package ru.usedesk.chat_sdk.internal.domain

import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import ru.usedesk.chat_sdk.external.IUsedeskChat
import ru.usedesk.chat_sdk.external.entity.*
import ru.usedesk.chat_sdk.external.entity.old.UsedeskOfflineForm
import ru.usedesk.chat_sdk.internal._entity.ChatInited
import ru.usedesk.chat_sdk.internal.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.internal.data.repository.configuration.IUserInfoRepository
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import toothpick.InjectConstructor
import java.util.*

@InjectConstructor
internal class ChatInteractor(
        private val configuration: UsedeskChatConfiguration,
        private val actionListener: IUsedeskActionListener,
        private val userInfoRepository: IUserInfoRepository,
        private val apiRepository: IApiRepository
) : IUsedeskChat {

    private var token: String? = null
    private var needSetEmail = false
    private val messageIds: MutableSet<Long> = HashSet()

    @Throws(UsedeskException::class)
    override fun connect() {
        try {
            val configuration = userInfoRepository.getConfiguration()
            if (this.configuration.email == configuration.email && this.configuration.companyId == configuration.companyId) {
                token = userInfoRepository.getToken()
            }
            if (this.configuration.clientName != configuration.clientName
                    || this.configuration.clientPhoneNumber != configuration.clientPhoneNumber
                    || this.configuration.clientAdditionalId != configuration.clientAdditionalId) {
                needSetEmail = true
            }
        } catch (e: UsedeskDataNotFoundException) {
            e.printStackTrace()
        }
        val eventListener = object : IApiRepository.EventListener {
            override fun onConnected() {
                actionListener.onConnected()
            }

            override fun onDisconnected() {
                actionListener.onDisconnected()
            }

            override fun onTokenError() {
                userInfoRepository.setToken(null)
                try {
                    token?.also {
                        apiRepository.init(configuration, it)
                    }
                } catch (e: UsedeskException) {
                    actionListener.onException(e)
                }
            }

            override fun onFeedback() {
                actionListener.onFeedbackReceived()
            }

            override fun onException(exception: Exception) {
                actionListener.onException(exception)
            }

            override fun onChatInited(chatInited: ChatInited) {
                this@ChatInteractor.onChatInited(chatInited)
            }

            override fun onNewChatItems(newChatItems: List<UsedeskChatItem>) {
                this@ChatInteractor.onNewChatItems(newChatItems)
            }
        }
        apiRepository.connect(
                configuration.url,
                token,
                configuration,
                eventListener
        )
    }

    private fun onNewChatItems(chatItems: List<UsedeskChatItem>) {
        chatItems.filter {
            !messageIds.contains(it.id)
        }.forEach {
            messageIds.add(it.id)
            actionListener.onChatItemReceived(it)
        }
    }

    override fun disconnect() {
        apiRepository.disconnect()
    }

    @Throws(UsedeskException::class)
    override fun send(textMessage: String) {
        if (textMessage.isEmpty()) {
            return
        }
        token?.also {
            apiRepository.send(it, textMessage)
        }
    }

    @Throws(UsedeskException::class)
    override fun send(usedeskFileInfo: UsedeskFileInfo) {
        token?.also {
            apiRepository.send(it, usedeskFileInfo)
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
        }
    }

    @Throws(UsedeskException::class)
    override fun send(offlineForm: UsedeskOfflineForm) {
        apiRepository.send(configuration, offlineForm.copy(companyId = offlineForm.companyId
                ?: configuration.companyId))
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
        }
    }

    override fun sendRx(usedeskFileInfoList: List<UsedeskFileInfo>): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            send(usedeskFileInfoList)
            emitter.onComplete()
        }
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
        }
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
            actionListener.onException(e)
        }
    }

    private fun onChatInited(chatChatInited: ChatInited) {
        this.token = chatChatInited.token
        userInfoRepository.setToken(token)
        actionListener.onConnected()
        if (chatChatInited.waitingEmail) {
            needSetEmail = true
        }
        actionListener.onChatItemsReceived(chatChatInited.messages)
        if (chatChatInited.noOperators) {
            actionListener.onOfflineFormExpected(configuration)
        }

        if (needSetEmail) {
            sendUserEmail()
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
    }
}