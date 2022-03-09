package ru.usedesk.chat_sdk.domain

import android.net.Uri
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.entity.ChatInited
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.entity.UsedeskEvent
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.utils.UsedeskRxUtil.safeCompletableIo
import ru.usedesk.common_sdk.utils.UsedeskRxUtil.safeSingleIo
import java.util.*
import java.util.concurrent.TimeUnit

internal class ChatInteractor(
    private val configuration: UsedeskChatConfiguration,
    private val userInfoRepository: IUserInfoRepository,
    private val apiRepository: IApiRepository,
    private val cachedMessages: ICachedMessagesInteractor
) : IUsedeskChat {

    private val ioScheduler = Schedulers.io()
    private var token: String? = null
    private var initClientMessage: String? = configuration.clientInitMessage
    private var initClientOfflineForm: String? = null

    private var actionListeners = mutableSetOf<IUsedeskActionListener>()
    private var actionListenersRx = mutableSetOf<IUsedeskActionListenerRx>()

    private val connectionStateSubject =
        BehaviorSubject.createDefault(UsedeskConnectionState.CONNECTING)
    private val clientTokenSubject = BehaviorSubject.create<String>()
    private val messagesSubject = BehaviorSubject.create<List<UsedeskMessage>>()
    private val messageSubject = BehaviorSubject.create<UsedeskMessage>()
    private val newMessageSubject = PublishSubject.create<UsedeskMessage>()
    private val messageUpdateSubject = PublishSubject.create<UsedeskMessage>()
    private val messageRemovedSubject = PublishSubject.create<UsedeskMessage>()
    private val offlineFormExpectedSubject = BehaviorSubject.create<UsedeskOfflineFormSettings>()
    private val feedbackSubject = BehaviorSubject.create<UsedeskEvent<Any?>>()
    private val exceptionSubject = BehaviorSubject.create<Exception>()

    private var reconnectDisposable: Disposable? = null
    private val listenersDisposables = mutableListOf<Disposable>()
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val jobs = mutableListOf<Job>()
    private val jobsMutex = Mutex()

    private var lastMessages = listOf<UsedeskMessage>()

    private var chatInited: ChatInited? = null
    private var offlineFormToChat = false

    private var initedNotSentMessages = listOf<UsedeskMessage>()

    private var additionalFieldsNeeded: Boolean = true
    private var avatarSendNeeded: Boolean = false

    init {
        listenersDisposables.apply {
            add(connectionStateSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onConnectionState(it)
                }
            })

            add(clientTokenSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onClientTokenReceived(it)
                }
            })

            add(messagesSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onMessagesReceived(it)
                }
            })

            add(messageSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onMessageReceived(it)
                }
            })

            add(newMessageSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onNewMessageReceived(it)
                }
            })

            add(messageUpdateSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onMessageUpdated(it)
                }
            })

            add(messageRemovedSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onMessageRemoved()
                }
            })

            add(offlineFormExpectedSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onOfflineFormExpected(it)
                }
            })

            add(feedbackSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onFeedbackReceived()
                }
            })

            add(exceptionSubject.subscribe {
                actionListeners.forEach { listener ->
                    listener.onException(it)
                }
            })
        }
    }

    private val eventListener = object : IApiRepository.EventListener {
        override fun onConnected() {
            connectionStateSubject.onNext(UsedeskConnectionState.CONNECTED)
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

            connectionStateSubject.onNext(UsedeskConnectionState.DISCONNECTED)
        }

        override fun onTokenError() {
            try {
                apiRepository.init(configuration, token)
            } catch (e: UsedeskException) {
                onException(e)
            }
        }

        override fun onFeedback() {
            feedbackSubject.onNext(UsedeskSingleLifeEvent(null))
        }

        override fun onException(exception: Exception) {
            exceptionSubject.onNext(exception)
        }

        @Synchronized
        override fun onChatInited(chatInited: ChatInited) {
            this@ChatInteractor.chatInited = chatInited
            this@ChatInteractor.onChatInited(chatInited)
        }

        @Synchronized
        override fun onMessagesReceived(newMessages: List<UsedeskMessage>) {
            this@ChatInteractor.onMessagesNew(newMessages, false)
        }

        @Synchronized
        override fun onMessageUpdated(message: UsedeskMessage) {
            this@ChatInteractor.onMessageUpdate(message)
        }

        override fun onOfflineForm(
            offlineFormSettings: UsedeskOfflineFormSettings,
            chatInited: ChatInited
        ) {
            this@ChatInteractor.chatInited = chatInited
            this@ChatInteractor.offlineFormToChat =
                offlineFormSettings.workType == UsedeskOfflineFormSettings.WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT
            offlineFormExpectedSubject.onNext(offlineFormSettings)
        }

        override fun onSetEmailSuccess() {
            sendInitMessage()
        }
    }

    private fun sendInitMessage() {
        val initMessage = initClientOfflineForm ?: initClientMessage
        initMessage?.let {
            try {
                send(it)
                initClientMessage = null
                initClientOfflineForm = null
            } catch (e: Exception) {
                //nothing
            }
        }
    }

    override fun connect() {
        val curState = connectionStateSubject.value
        if (curState != UsedeskConnectionState.CONNECTED) {
            if (curState != UsedeskConnectionState.CONNECTING) {
                connectionStateSubject.onNext(UsedeskConnectionState.RECONNECTING)
            }
            reconnectDisposable?.dispose()
            reconnectDisposable = null
            token = if (!isStringEmpty(this.configuration.clientToken)) {
                this.configuration.clientToken
            } else {
                userInfoRepository.getConfigurationNullable(configuration)?.clientToken
            }
            apiRepository.connect(
                this.configuration.urlChat,
                token,
                this.configuration,
                eventListener
            )
        }
    }

    private fun isStringEmpty(text: String?): Boolean {
        return text?.isEmpty() != false
    }

    private fun onMessageUpdate(message: UsedeskMessage) {
        runBlocking {
            jobsMutex.withLock {
                lastMessages = lastMessages.map {
                    if ((it is UsedeskMessageClient &&
                                message is UsedeskMessageClient &&
                                it.localId == message.localId)
                        || (it is UsedeskMessageAgent &&
                                message is UsedeskMessageAgent &&
                                it.id == message.id
                                )
                    ) {
                        message
                    } else {
                        it
                    }
                }
                messagesSubject.onNext(lastMessages)
                messageUpdateSubject.onNext(message)
            }
        }
    }

    private fun onMessageRemove(message: UsedeskMessage) {
        lastMessages = lastMessages.filter {
            it.id != message.id
        }
        messagesSubject.onNext(lastMessages)
        messageRemovedSubject.onNext(message)
    }

    private fun onMessagesNew(
        messages: List<UsedeskMessage>,
        isInited: Boolean
    ) {
        lastMessages = lastMessages + messages
        messages.forEach { message ->
            messageSubject.onNext(message)
            if (!isInited) {
                newMessageSubject.onNext(message)
            }
        }
        messagesSubject.onNext(lastMessages)
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
            connectionStateSubject,
            clientTokenSubject,
            messageSubject,
            newMessageSubject,
            messagesSubject,
            messageUpdateSubject,
            messageRemovedSubject,
            offlineFormExpectedSubject,
            feedbackSubject,
            exceptionSubject
        )
    }

    override fun removeActionListener(listener: IUsedeskActionListenerRx) {
        actionListenersRx.remove(listener)
        listener.onDispose()
    }

    override fun isNoListeners(): Boolean {
        return actionListeners.isEmpty() && actionListenersRx.isEmpty()
    }

    override fun send(textMessage: String) {
        val message = textMessage.trim()
        if (message.isNotEmpty()) {
            val sendingMessage = createSendingMessage(message)
            eventListener.onMessagesReceived(listOf(sendingMessage))
            sendText(sendingMessage)
        }
    }

    private fun sendText(sendingMessage: UsedeskMessageClientText) {
        try {
            sendAdditionalFieldsIfNeeded()
            sendCached(sendingMessage)
        } catch (e: Exception) {
            onMessageSendingFailed(sendingMessage)
            throw e
        }
    }

    private fun sendAdditionalFieldsIfNeeded() {
        if (additionalFieldsNeeded) {
            additionalFieldsNeeded = false
            if (configuration.additionalFields.isNotEmpty() ||
                configuration.additionalNestedFields.isNotEmpty()
            ) {
                try {
                    apiRepository.send(
                        token,
                        configuration,
                        configuration.additionalFields,
                        configuration.additionalNestedFields
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    additionalFieldsNeeded = true
                    throw e
                }
            }
        }
    }

    override fun send(usedeskFileInfoList: List<UsedeskFileInfo>) {
        var exc: Exception? = null
        val sendingFiles = usedeskFileInfoList.map { usedeskFileInfo ->
            createSendingMessage(usedeskFileInfo).also {
                eventListener.onMessagesReceived(listOf(it))
            }
        }

        sendingFiles.forEach { sendingMessage ->
            try {
                sendFile(sendingMessage)
            } catch (e: Exception) {
                exc = e
            }
        }

        exc?.let {
            throw it
        }
    }

    private fun sendFile(sendingMessage: UsedeskMessageFile) {
        sendingMessage as UsedeskMessageClient
        try {
            sendAdditionalFieldsIfNeeded()
            sendCached(sendingMessage)
        } catch (e: Exception) {
            onMessageSendingFailed(sendingMessage)
            throw e
        }
    }

    private fun sendCached(fileMessage: UsedeskMessageFile) {
        try {
            cachedMessages.addNotSentMessage(fileMessage as UsedeskMessageClient)
            val cachedUri = runBlocking {
                val uri = Uri.parse(fileMessage.file.content)
                val deferredCachedUri = cachedMessages.getCachedFileAsync(uri)
                deferredCachedUri.await()
            }
            val newFile = fileMessage.file.copy(content = cachedUri.toString())
            val cachedNotSentMessage = when (fileMessage) {
                is UsedeskMessageClientAudio -> {
                    UsedeskMessageClientAudio(
                        fileMessage.id,
                        fileMessage.createdAt,
                        newFile,
                        fileMessage.status,
                        fileMessage.localId
                    )
                }
                is UsedeskMessageClientVideo -> {
                    UsedeskMessageClientVideo(
                        fileMessage.id,
                        fileMessage.createdAt,
                        newFile,
                        fileMessage.status,
                        fileMessage.localId
                    )
                }
                is UsedeskMessageClientImage -> {
                    UsedeskMessageClientImage(
                        fileMessage.id,
                        fileMessage.createdAt,
                        newFile,
                        fileMessage.status,
                        fileMessage.localId
                    )
                }
                else -> {
                    UsedeskMessageClientFile(
                        fileMessage.id,
                        fileMessage.createdAt,
                        newFile,
                        fileMessage.status,
                        fileMessage.localId
                    )
                }
            }
            cachedMessages.updateNotSentMessage(cachedNotSentMessage)
            eventListener.onMessageUpdated(cachedNotSentMessage)
            apiRepository.send(
                configuration,
                token!!,
                UsedeskFileInfo(
                    cachedUri,
                    cachedNotSentMessage.file.type,
                    cachedNotSentMessage.file.name
                ),
                cachedNotSentMessage.localId
            )
            cachedMessages.removeNotSentMessage(cachedNotSentMessage)
            runBlocking {
                cachedMessages.removeFileFromCache(Uri.parse(fileMessage.file.content))
            }
        } catch (e: Exception) {
            onMessageSendingFailed(fileMessage as UsedeskMessageClient)
            throw e
        }
    }

    private fun sendCached(cachedMessage: UsedeskMessageClientText) {
        try {
            cachedMessages.addNotSentMessage(cachedMessage)
            apiRepository.send(cachedMessage)
            cachedMessages.removeNotSentMessage(cachedMessage)
        } catch (e: Exception) {
            onMessageSendingFailed(cachedMessage)
            throw e
        }
    }

    private fun onMessageSendingFailed(sendingMessage: UsedeskMessageClient) {
        when (sendingMessage) {
            is UsedeskMessageClientText -> {
                UsedeskMessageClientText(
                    sendingMessage.id,
                    sendingMessage.createdAt,
                    sendingMessage.text,
                    UsedeskMessageClient.Status.SEND_FAILED
                )
            }
            is UsedeskMessageClientFile -> {
                UsedeskMessageClientFile(
                    sendingMessage.id,
                    sendingMessage.createdAt,
                    sendingMessage.file,
                    UsedeskMessageClient.Status.SEND_FAILED
                )
            }
            is UsedeskMessageClientImage -> {
                UsedeskMessageClientImage(
                    sendingMessage.id,
                    sendingMessage.createdAt,
                    sendingMessage.file,
                    UsedeskMessageClient.Status.SEND_FAILED
                )
            }
            is UsedeskMessageClientVideo -> {
                UsedeskMessageClientVideo(
                    sendingMessage.id,
                    sendingMessage.createdAt,
                    sendingMessage.file,
                    UsedeskMessageClient.Status.SEND_FAILED
                )
            }
            is UsedeskMessageClientAudio -> {
                UsedeskMessageClientAudio(
                    sendingMessage.id,
                    sendingMessage.createdAt,
                    sendingMessage.file,
                    UsedeskMessageClient.Status.SEND_FAILED
                )
            }
            else -> null
        }?.let {
            onMessageUpdate(it)
        }
    }

    override fun send(agentMessage: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        apiRepository.send(agentMessage.id, feedback)

        onMessageUpdate(
            UsedeskMessageAgentText(
                agentMessage.id,
                agentMessage.createdAt,
                agentMessage.text,
                agentMessage.buttons,
                false,
                feedback,
                agentMessage.name,
                agentMessage.avatar
            )
        )
    }

    override fun send(offlineForm: UsedeskOfflineForm) {
        if (offlineFormToChat) {
            offlineForm.run {
                val fields = offlineForm.fields.filter { field ->
                    field.value.isNotEmpty()
                }.map { field ->
                    field.value
                }
                val strings =
                    listOf(clientName, clientEmail, topic) + fields + offlineForm.message
                initClientOfflineForm = strings.joinToString(separator = "\n")
                chatInited?.let { onChatInited(it) }
            }
        } else {
            apiRepository.send(configuration, configuration.getCompanyAndChannel(), offlineForm)
        }
    }

    override fun sendAgain(id: Long) {
        val message = lastMessages.firstOrNull {
            it.id == id
        }
        if (message is UsedeskMessageClient
            && message.status == UsedeskMessageClient.Status.SEND_FAILED
        ) {
            when (message) {
                is UsedeskMessageClientText -> {
                    val sendingMessage = UsedeskMessageClientText(
                        message.id,
                        message.createdAt,
                        message.text,
                        UsedeskMessageClient.Status.SENDING
                    )
                    onMessageUpdate(sendingMessage)
                    sendText(sendingMessage)
                }
                is UsedeskMessageClientImage -> {
                    val sendingMessage = UsedeskMessageClientImage(
                        message.id,
                        message.createdAt,
                        message.file,
                        UsedeskMessageClient.Status.SENDING
                    )
                    onMessageUpdate(sendingMessage)
                    sendFile(sendingMessage)
                }
                is UsedeskMessageClientVideo -> {
                    val sendingMessage = UsedeskMessageClientVideo(
                        message.id,
                        message.createdAt,
                        message.file,
                        UsedeskMessageClient.Status.SENDING
                    )
                    onMessageUpdate(sendingMessage)
                    sendFile(sendingMessage)
                }
                is UsedeskMessageClientAudio -> {
                    val sendingMessage = UsedeskMessageClientAudio(
                        message.id,
                        message.createdAt,
                        message.file,
                        UsedeskMessageClient.Status.SENDING
                    )
                    onMessageUpdate(sendingMessage)
                    sendFile(sendingMessage)
                }
                is UsedeskMessageClientFile -> {
                    val sendingMessage = UsedeskMessageClientFile(
                        message.id,
                        message.createdAt,
                        message.file,
                        UsedeskMessageClient.Status.SENDING
                    )
                    onMessageUpdate(sendingMessage)
                    sendFile(sendingMessage)
                }
            }
        }
    }

    override fun removeMessage(id: Long) {
        cachedMessages.getNotSentMessages().firstOrNull {
            it.localId == id
        }?.let {
            cachedMessages.removeNotSentMessage(it)
            onMessageRemove(it as UsedeskMessage)
        }
    }

    override fun removeMessageRx(id: Long): Completable {
        return safeCompletableIo(ioScheduler) {
            removeMessage(id)
        }
    }

    @Synchronized
    override fun setMessageDraft(messageDraft: UsedeskMessageDraft) {
        runBlocking {
            cachedMessages.setMessageDraft(messageDraft, true)
        }
    }

    override fun setMessageDraftRx(messageDraft: UsedeskMessageDraft): Completable {
        return safeCompletableIo(ioScheduler) {
            setMessageDraft(messageDraft)
        }
    }

    override fun getMessageDraft(): UsedeskMessageDraft {
        return runBlocking {
            cachedMessages.getMessageDraft()
        }
    }

    override fun getMessageDraftRx(): Single<UsedeskMessageDraft> {
        return safeSingleIo(ioScheduler) {
            getMessageDraft()
        }
    }

    override fun sendMessageDraft() {
        runBlocking {
            val messageDraft = cachedMessages.setMessageDraft(
                UsedeskMessageDraft(),
                false
            )

            val sendingMessages = mutableListOf<UsedeskMessage>()

            val message = messageDraft.text.trim()
            if (message.isNotEmpty()) {
                val sendingMessage = createSendingMessage(message)
                sendingMessages.add(sendingMessage)
            }

            sendingMessages.addAll(messageDraft.files.map {
                createSendingMessage(it)
            })

            eventListener.onMessagesReceived(sendingMessages)
            sendAdditionalFieldsIfNeeded()

            jobsMutex.withLock {
                jobs.addAll(sendingMessages.mapNotNull { msg ->
                    when (msg) {
                        is UsedeskMessageClientText -> {
                            ioScope.launchSafe({
                                sendCached(msg)
                            })
                        }
                        is UsedeskMessageFile -> {
                            ioScope.launchSafe({
                                sendCached(msg)
                            })
                        }
                        else -> {
                            null
                        }
                    }
                })
            }
        }
    }

    override fun sendMessageDraftRx(): Completable {
        return safeCompletableIo(ioScheduler) {
            sendMessageDraft()
        }
    }

    private fun createSendingMessage(text: String): UsedeskMessageClientText {
        val localId = cachedMessages.getNextLocalId()
        val calendar = Calendar.getInstance()
        return UsedeskMessageClientText(
            localId,
            calendar,
            text,
            UsedeskMessageClient.Status.SENDING
        )
    }

    private fun createSendingMessage(fileInfo: UsedeskFileInfo): UsedeskMessageFile {
        val localId = cachedMessages.getNextLocalId()
        val calendar = Calendar.getInstance()
        val file = UsedeskFile.create(
            fileInfo.uri.toString(),
            fileInfo.type,
            "",
            fileInfo.name
        )
        return when {
            fileInfo.isImage() -> {
                UsedeskMessageClientImage(
                    localId,
                    calendar,
                    file,
                    UsedeskMessageClient.Status.SENDING
                )
            }
            fileInfo.isVideo() -> {
                UsedeskMessageClientVideo(
                    localId,
                    calendar,
                    file,
                    UsedeskMessageClient.Status.SENDING
                )
            }
            fileInfo.isAudio() -> {
                UsedeskMessageClientAudio(
                    localId,
                    calendar,
                    file,
                    UsedeskMessageClient.Status.SENDING
                )
            }
            else -> {
                UsedeskMessageClientFile(
                    localId,
                    calendar,
                    file,
                    UsedeskMessageClient.Status.SENDING
                )
            }
        }
    }

    override fun connectRx(): Completable {
        return safeCompletableIo(ioScheduler) {
            connect()
        }
    }

    override fun sendRx(textMessage: String): Completable {
        return safeCompletableIo(ioScheduler) {
            send(textMessage)
        }
    }

    override fun sendRx(usedeskFileInfoList: List<UsedeskFileInfo>): Completable {
        return safeCompletableIo(ioScheduler) {
            send(usedeskFileInfoList)
        }
    }

    override fun sendRx(
        agentMessage: UsedeskMessageAgentText,
        feedback: UsedeskFeedback
    ): Completable {
        return safeCompletableIo(ioScheduler) {
            send(agentMessage, feedback)
        }
    }

    override fun sendRx(offlineForm: UsedeskOfflineForm): Completable {
        return safeCompletableIo(ioScheduler) {
            send(offlineForm)
        }
    }

    override fun sendAgainRx(id: Long): Completable {
        return safeCompletableIo(ioScheduler) {
            sendAgain(id)
        }
    }

    override fun disconnectRx(): Completable {
        return safeCompletableIo(ioScheduler) {
            disconnect()
        }
    }

    override fun release() {
        listenersDisposables.forEach {
            it.dispose()
        }
        runBlocking {
            jobsMutex.withLock {
                jobs.forEach {
                    it.cancel()
                }
            }
        }
        disconnect()
    }

    override fun releaseRx(): Completable {
        return safeCompletableIo(ioScheduler) {
            release()
        }
    }

    private fun sendUserEmail() {
        try {
            token?.let {
                apiRepository.send(
                    it,
                    configuration.clientEmail,
                    configuration.clientName,
                    configuration.clientNote,
                    configuration.clientPhoneNumber,
                    configuration.clientAdditionalId
                )
            }
        } catch (e: UsedeskException) {
            exceptionSubject.onNext(e)
        }
    }

    private fun onChatInited(chatInited: ChatInited) {
        this.token = chatInited.token
        clientTokenSubject.onNext(chatInited.token)

        val oldConfiguration = userInfoRepository.getConfigurationNullable(configuration)

        if (initClientOfflineForm != null ||
            oldConfiguration?.clientInitMessage == initClientMessage
        ) {
            initClientMessage = null
        }
        avatarSendNeeded = false //oldConfiguration?.clientAvatar != configuration.clientAvatar

        userInfoRepository.setConfiguration(configuration.copy(clientToken = token))

        val ids = lastMessages.map {
            it.id
        }
        val filteredMessages = chatInited.messages.filter {
            it.id !in ids
        }
        val notSentMessages = cachedMessages.getNotSentMessages().map {
            it as UsedeskMessage
        }
        val filteredNotSentMessages = notSentMessages.filter {
            it.id !in ids
        }
        val needToResendMessages = lastMessages.isNotEmpty()
        onMessagesNew(filteredMessages + filteredNotSentMessages, true)

        if (chatInited.waitingEmail) {
            sendUserEmail()
        } else {
            eventListener.onSetEmailSuccess()
        }
        if (needToResendMessages) {
            val initedNotSentIds = initedNotSentMessages.map { it.id }
            notSentMessages.filter {
                it.id !in initedNotSentIds
            }.forEach {
                listenersDisposables.add(
                    sendAgainRx(it.id).subscribe({}, { throwable ->
                        throwable.printStackTrace()
                    })
                )
            }
        } else {
            initedNotSentMessages = notSentMessages
        }
    }

    private fun CoroutineScope.launchSafe(
        onRun: () -> Unit,
        onThrowable: (Throwable) -> Unit = { it.printStackTrace() }
    ): Job {
        return launch {
            try {
                onRun()
            } catch (e: Throwable) {
                onThrowable(e)
            }
        }
    }
}