package ru.usedesk.chat_sdk.internal.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import io.reactivex.Completable
import io.reactivex.CompletableEmitter
import ru.usedesk.chat_sdk.external.IUsedeskChat
import ru.usedesk.chat_sdk.external.entity.*
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.InitChatResponse
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.Setup
import ru.usedesk.chat_sdk.internal.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.internal.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.internal.domain.entity.OnMessageListener
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import toothpick.InjectConstructor
import java.util.*

@InjectConstructor
class ChatInteractor(
        private val context: Context,
        private val configuration: UsedeskChatConfiguration,
        private val actionListener: IUsedeskActionListener,
        private val userInfoRepository: IUserInfoRepository,
        private val apiRepository: IApiRepository
) : IUsedeskChat {

    private var token: String? = null
    private var needSetEmail = false
    private val messageIds: MutableSet<String> = HashSet()

    private fun <T> equals(a: T?, b: T?): Boolean {
        return a == null && b == null || a != null && a == b
    }

    @Throws(UsedeskException::class)
    override fun connect() {
        try {
            val configuration = userInfoRepository.getConfiguration()
            if (this.configuration.email == configuration.email && this.configuration.companyId == configuration.companyId) {
                token = userInfoRepository.getToken()
            }
            if (!equals(this.configuration.clientName, configuration.clientName)
                    || !equals(this.configuration.clientPhoneNumber, configuration.clientPhoneNumber)
                    || !equals(this.configuration.clientAdditionalId, configuration.clientAdditionalId)) {
                needSetEmail = true
            }
        } catch (e: UsedeskDataNotFoundException) {
            e.printStackTrace()
        }
        apiRepository.connect(configuration.url, actionListener, onMessageListener)
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
    override fun send(feedback: UsedeskFeedback) {
        token?.also {
            apiRepository.send(it, feedback)
        }
    }

    @Throws(UsedeskException::class)
    override fun send(offlineForm: UsedeskOfflineForm) {
        apiRepository.send(configuration, offlineForm.copy(companyId = offlineForm.companyId
                ?: configuration.companyId))
    }

    @Throws(UsedeskException::class)
    override fun send(messageButton: UsedeskMessageButton) {
        if (messageButton.url.isEmpty()) {
            send(messageButton.text)
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(messageButton.url))
            browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(browserIntent)
        }
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

    override fun sendRx(feedback: UsedeskFeedback): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            send(feedback)
            emitter.onComplete()
        }
    }

    override fun sendRx(offlineForm: UsedeskOfflineForm): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            send(offlineForm)
            emitter.onComplete()
        }
    }

    override fun sendRx(messageButton: UsedeskMessageButton): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            send(messageButton)
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

    private fun parseNewMessageResponse(message: UsedeskMessage) {
        val hasText = !TextUtils.isEmpty(message.text)
        val hasFile = message.file != null
        if ((hasText || hasFile) && !messageIds.contains(message.id) && message.id != null) {
            messageIds.add(message.id)
            actionListener.onMessageReceived(message)
        }
    }

    private fun parseInitResponse(token: String, setup: Setup?) {
        this.token = token
        userInfoRepository.setToken(token)
        actionListener.onConnected()
        if (setup != null) {
            if (setup.waitingEmail) {
                needSetEmail = true
            }
            actionListener.onMessagesReceived(setup.getMessages())
            if (setup.isNoOperators) {
                actionListener.onOfflineFormExpected(configuration)
            }
        } else {
            needSetEmail = true
        }
        if (needSetEmail) {
            sendUserEmail()
        }
        val initClientMessage = try {
            userInfoRepository.getConfiguration().initClientMessage
        } catch (ignore: UsedeskException) {
            null
        }
        try {
            if (!equals(initClientMessage, configuration.initClientMessage)) {
                send(configuration.initClientMessage!!)
            }
        } catch (ignore: UsedeskException) {
        }
        userInfoRepository.setConfiguration(configuration)
    }

    private val onMessageListener = object : OnMessageListener {
        override fun onNew(message: UsedeskMessage) {
            parseNewMessageResponse(message)
        }

        override fun onFeedback() {
            actionListener.onFeedbackReceived()
        }

        override fun onInit(initChatResponse: InitChatResponse) {
            if (initChatResponse.token != null && initChatResponse.setup != null) {
                parseInitResponse(initChatResponse.token, initChatResponse.setup)
            }
        }

        override fun onInitChat() {
            try {
                apiRepository.init(configuration, token)
            } catch (e: UsedeskException) {
                actionListener.onException(e)
            }
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
    }
}