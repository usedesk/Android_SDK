package ru.usedesk.chat_sdk.domain

import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.InitChatResponse
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.LoadPreviousMessageResult
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.domain.IUsedeskChat.CreateChatResult
import ru.usedesk.chat_sdk.domain.IUsedeskChat.Model
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form.Field
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings.WorkType
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.common_sdk.entity.exceptions.UsedeskException
import java.util.*
import javax.inject.Inject

internal class ChatInteractor @Inject constructor(
    private val configuration: UsedeskChatConfiguration,
    private val userInfoRepository: IUserInfoRepository,
    private val apiRepository: IApiRepository,
    private val cachedMessages: ICachedMessagesInteractor
) : IUsedeskChat {

    private var token: String? = null
    private var initClientMessage: String? = configuration.clientInitMessage
    private var initClientOfflineForm: String? = null

    private val modelFlow = MutableStateFlow(Model())

    private val formLoadSet = mutableSetOf<Long>()

    private class ActionListeners : IUsedeskActionListener {
        private val listenersMutex = Mutex()
        private var listeners = mutableSetOf<IUsedeskActionListener>()

        fun add(listener: IUsedeskActionListener) {
            runBlocking {
                listenersMutex.withLock {
                    listeners.add(listener)
                }
            }
        }

        fun remove(listener: IUsedeskActionListener) {
            runBlocking {
                listenersMutex.withLock {
                    listeners.remove(listener)
                }
            }
        }

        fun isNoListeners() = runBlocking { listenersMutex.withLock { listeners } }.isEmpty()

        override fun onConnectionState(connectionState: UsedeskConnectionState) {
            listeners.forEach { it.onConnectionState(connectionState) }
        }

        override fun onClientTokenReceived(clientToken: String) {
            listeners.forEach { it.onClientTokenReceived(clientToken) }
        }

        override fun onMessageReceived(message: UsedeskMessage) {
            listeners.forEach { it.onMessageReceived(message) }
        }

        override fun onNewMessageReceived(message: UsedeskMessage) {
            listeners.forEach { it.onNewMessageReceived(message) }
        }

        override fun onMessagesReceived(messages: List<UsedeskMessage>) {
            listeners.forEach { it.onMessagesReceived(messages) }
        }

        override fun onMessageUpdated(message: UsedeskMessage) {
            listeners.forEach { it.onMessageUpdated(message) }
        }

        override fun onMessageRemoved() {
            listeners.forEach { it.onMessageRemoved() }
        }

        override fun onFeedbackReceived() {
            listeners.forEach { it.onFeedbackReceived() }
        }

        override fun onOfflineFormExpected(offlineFormSettings: UsedeskOfflineFormSettings) {
            listeners.forEach { it.onOfflineFormExpected(offlineFormSettings) }
        }

        override fun onException(usedeskException: Exception) {
            listeners.forEach { it.onException(usedeskException) }
        }
    }

    private val actionListeners = ActionListeners()

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reconnectJob: Job? = null
    private val eventMutex = Mutex()
    private val firstMessageMutex = Mutex()
    private var firstMessageLock: Mutex? = null

    private var chatInited: ChatInited? = null
    private var offlineFormToChat = false

    private var initedNotSentMessages = listOf<UsedeskMessage>()

    private var additionalFieldsNeeded: Boolean = true

    private val oldMutex = Mutex()
    private var oldMessagesLoadDeferred: Deferred<Boolean>? = null

    var oldModel: Model? = null
    private fun Model.onModelUpdated(oldModel: Model?, listener: IUsedeskActionListener) {
        if (oldModel?.connectionState != connectionState) {
            listener.onConnectionState(connectionState)
        }
        if (oldModel?.clientToken != clientToken && clientToken != null) {
            listener.onClientTokenReceived(clientToken)
        }
        if (oldModel?.offlineFormSettings != offlineFormSettings &&
            offlineFormSettings != null
        ) {
            listener.onOfflineFormExpected(offlineFormSettings)
        }
        if (oldModel?.feedbackEvent != feedbackEvent) {
            listener.onFeedbackReceived()
        }
        if (oldModel?.messages != messages) {
            listener.onMessagesReceived(messages)
            messages.forEach { message ->
                val oldMessage = oldModel?.messages?.firstOrNull { it.id == message.id }
                when {
                    oldMessage == null -> {
                        if (!inited) {
                            listener.onNewMessageReceived(message)
                        }
                        listener.onMessageReceived(message)
                    }
                    oldMessage != message -> listener.onMessageUpdated(message)
                }
            }
            oldModel?.messages?.forEach { oldMessage ->
                if (messages.all { message -> message.id != oldMessage.id }) {
                    listener.onMessageRemoved()
                }
            }
        }
    }

    init {
        ioScope.launch {
            modelFlow.collect { model ->
                model.onModelUpdated(oldModel, actionListeners)
                oldModel = model
            }
        }
    }

    private val eventListener = object : IApiRepository.EventListener {
        override fun onConnected() {
            runBlocking {
                setModel {
                    copy(connectionState = UsedeskConnectionState.CONNECTED)
                }
            }
        }

        override fun onDisconnected() {
            runBlocking {
                reconnectJob?.cancel()
                reconnectJob = ioScope.launch {
                    delay(5000)
                    yield()
                    connect()
                }
                setModel {
                    copy(
                        connectionState = UsedeskConnectionState.DISCONNECTED
                    )
                }
            }
        }

        override fun onTokenError() {
            try {
                apiRepository.init(configuration, token)
            } catch (e: UsedeskException) {
                onException(e)
            }
        }

        override fun onFeedback() {
            runBlocking {
                setModel {
                    copy(feedbackEvent = UsedeskSingleLifeEvent(Unit))
                }
            }
        }

        override fun onException(exception: Exception) {
            actionListeners.onException(exception)
        }

        override fun onChatInited(chatInited: ChatInited) {
            this@ChatInteractor.chatInited = chatInited
            this@ChatInteractor.onChatInited(chatInited)
        }

        override fun onMessagesOldReceived(oldMessages: List<UsedeskMessage>) {
            this@ChatInteractor.onMessagesNew(
                old = oldMessages,
                isInited = false
            )
        }

        override fun onMessagesNewReceived(newMessages: List<UsedeskMessage>) {
            this@ChatInteractor.onMessagesNew(
                new = newMessages,
                isInited = false
            )
        }

        override fun onMessageUpdated(message: UsedeskMessage) {
            this@ChatInteractor.onMessageUpdate(message)
        }

        override fun onOfflineForm(
            offlineFormSettings: UsedeskOfflineFormSettings,
            chatInited: ChatInited
        ) {
            this@ChatInteractor.chatInited = chatInited
            this@ChatInteractor.offlineFormToChat =
                offlineFormSettings.workType == WorkType.ALWAYS_ENABLED_CALLBACK_WITH_CHAT
            runBlocking {
                setModel {
                    copy(offlineFormSettings = offlineFormSettings)
                }
            }
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

    override fun createChat(apiToken: String): CreateChatResult {
        val response = apiRepository.initChat(
            configuration,
            apiToken
        )
        return when (response) {
            is InitChatResponse.ApiError -> CreateChatResult.Error(response.code)
            is InitChatResponse.Done -> {
                userInfoRepository.setConfiguration(configuration.copy(clientToken = response.clientToken))
                CreateChatResult.Done(response.clientToken)
            }
        }
    }

    override fun connect() {
        runBlocking {
            setModel {
                copy(
                    connectionState = when (connectionState) {
                        UsedeskConnectionState.DISCONNECTED -> UsedeskConnectionState.RECONNECTING.apply {
                            reconnectJob?.cancel()
                            token = (configuration.clientToken
                                ?: userInfoRepository.getConfiguration(configuration)?.clientToken)
                                ?.ifEmpty { null }
                            ioScope.launch {
                                unlockFirstMessage()
                                resetFirstMessageLock()

                                apiRepository.connect(
                                    configuration.urlChat,
                                    token,
                                    configuration,
                                    eventListener
                                )
                            }
                        }
                        else -> connectionState
                    }
                )
            }
        }
    }

    private suspend fun setModel(onUpdate: Model.() -> Model): Model = eventMutex.withLock {
        modelFlow.value.onUpdate().also {
            modelFlow.value = it
        }
    }

    private fun onMessageUpdate(message: UsedeskMessage) {
        runBlocking {
            eventMutex.withLock {
                setModel {
                    copy(messages = messages.map {
                        when {
                            it is UsedeskMessageClient &&
                                    message is UsedeskMessageClient &&
                                    it.localId == message.localId || it is UsedeskMessageAgent &&
                                    message is UsedeskMessageAgent &&
                                    it.id == message.id -> message
                            else -> it
                        }
                    })
                }
            }
        }
    }

    private fun onMessageRemove(message: UsedeskMessage) {
        runBlocking {
            setModel {
                copy(
                    messages = messages.filter { it.id != message.id }
                )
            }
        }
    }

    private fun onMessagesNew(
        old: List<UsedeskMessage> = listOf(),
        new: List<UsedeskMessage> = listOf(),
        isInited: Boolean
    ) {
        runBlocking {
            setModel {
                copy(
                    messages = old + messages + new,
                    inited = isInited
                )
            }
        }
    }

    override fun disconnect() {
        reconnectJob?.cancel()
        ioScope.launch {
            apiRepository.disconnect()
        }
    }

    override fun addActionListener(listener: IUsedeskActionListener) {
        actionListeners.add(listener)
        modelFlow.value.apply {
            ioScope.launch {
                onModelUpdated(null, listener)
            }
        }
    }

    override fun removeActionListener(listener: IUsedeskActionListener) {
        actionListeners.remove(listener)
    }

    override fun isNoListeners(): Boolean = actionListeners.isNoListeners()

    override fun send(textMessage: String) {
        val message = textMessage.trim()
        if (message.isNotEmpty()) {
            val sendingMessage = createSendingMessage(message)
            eventListener.onMessagesNewReceived(listOf(sendingMessage))

            runBlocking {
                eventMutex.withLock {
                    ioScope.launchSafe({ sendCached(sendingMessage) })
                }
            }
        }
    }

    private fun sendText(sendingMessage: UsedeskMessageClientText) {
        try {
            sendCached(sendingMessage)
        } catch (e: Exception) {
            onMessageSendingFailed(sendingMessage)
            throw e
        }
    }

    private fun sendAdditionalFieldsIfNeededAsync() {
        runBlocking {
            eventMutex.withLock {
                if (additionalFieldsNeeded) {
                    additionalFieldsNeeded = false
                    if (configuration.additionalFields.isNotEmpty() ||
                        configuration.additionalNestedFields.isNotEmpty()
                    ) {
                        ioScope.launch {
                            try {
                                waitFirstMessage()
                                delay(3000)
                                apiRepository.send(
                                    token!!,
                                    configuration,
                                    configuration.additionalFields,
                                    configuration.additionalNestedFields
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                additionalFieldsNeeded = true
                                actionListeners.onException(e)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun send(usedeskFileInfoList: List<UsedeskFileInfo>) {
        val sendingMessages = usedeskFileInfoList.map(this@ChatInteractor::createSendingMessage)

        eventListener.onMessagesNewReceived(sendingMessages)

        runBlocking {
            eventMutex.withLock {
                sendingMessages.forEach { msg ->
                    ioScope.launchSafe({ sendCached(msg) })
                }
            }
        }
    }

    private fun sendFile(sendingMessage: UsedeskMessageFile) {
        sendingMessage as UsedeskMessageClient
        try {
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
                is UsedeskMessageClientAudio -> UsedeskMessageClientAudio(
                    fileMessage.id,
                    fileMessage.createdAt,
                    newFile,
                    fileMessage.status,
                    fileMessage.localId
                )
                is UsedeskMessageClientVideo -> UsedeskMessageClientVideo(
                    fileMessage.id,
                    fileMessage.createdAt,
                    newFile,
                    fileMessage.status,
                    fileMessage.localId
                )
                is UsedeskMessageClientImage -> UsedeskMessageClientImage(
                    fileMessage.id,
                    fileMessage.createdAt,
                    newFile,
                    fileMessage.status,
                    fileMessage.localId
                )
                else -> UsedeskMessageClientFile(
                    fileMessage.id,
                    fileMessage.createdAt,
                    newFile,
                    fileMessage.status,
                    fileMessage.localId
                )
            }
            cachedMessages.updateNotSentMessage(cachedNotSentMessage)
            eventListener.onMessageUpdated(cachedNotSentMessage)
            waitFirstMessage()
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
            unlockFirstMessage()
            cachedMessages.removeNotSentMessage(cachedNotSentMessage)
            runBlocking {
                cachedMessages.removeFileFromCache(Uri.parse(fileMessage.file.content))
            }
            sendAdditionalFieldsIfNeededAsync()
        } catch (e: Exception) {
            onMessageSendingFailed(fileMessage as UsedeskMessageClient)
            throw e
        }
    }

    private fun resetFirstMessageLock() {
        runBlocking {
            firstMessageMutex.withLock {
                firstMessageLock = Mutex()
            }
        }
    }

    private fun lockFirstMessage() {
        runBlocking {
            firstMessageMutex.withLock {
                firstMessageLock
            }?.apply {
                when {
                    !isLocked -> lock(this@ChatInteractor)
                    else -> waitFirstMessage()
                }
            }
        }
    }

    private fun unlockFirstMessage() {
        runBlocking {
            firstMessageMutex.withLock {
                firstMessageLock?.apply {
                    if (isLocked) {
                        delay(1000)
                        unlock(this@ChatInteractor)
                    }
                    firstMessageLock = null
                    println()
                }
            }
        }
    }

    private fun waitFirstMessage() {
        runBlocking {
            firstMessageLock?.withLock {}
        }
    }

    private fun sendCached(cachedMessage: UsedeskMessageClientText) {
        try {
            cachedMessages.addNotSentMessage(cachedMessage)
            lockFirstMessage()
            apiRepository.send(cachedMessage)
            cachedMessages.removeNotSentMessage(cachedMessage)
            sendAdditionalFieldsIfNeededAsync()
        } catch (e: Exception) {
            onMessageSendingFailed(cachedMessage)
            throw e
        } finally {
            unlockFirstMessage()
        }
    }

    private fun onMessageSendingFailed(sendingMessage: UsedeskMessageClient) {
        when (sendingMessage) {
            is UsedeskMessageClientText -> UsedeskMessageClientText(
                sendingMessage.id,
                sendingMessage.createdAt,
                sendingMessage.text,
                sendingMessage.convertedText,
                UsedeskMessageClient.Status.SEND_FAILED
            )
            is UsedeskMessageClientFile -> UsedeskMessageClientFile(
                sendingMessage.id,
                sendingMessage.createdAt,
                sendingMessage.file,
                UsedeskMessageClient.Status.SEND_FAILED
            )
            is UsedeskMessageClientImage -> UsedeskMessageClientImage(
                sendingMessage.id,
                sendingMessage.createdAt,
                sendingMessage.file,
                UsedeskMessageClient.Status.SEND_FAILED
            )
            is UsedeskMessageClientVideo -> UsedeskMessageClientVideo(
                sendingMessage.id,
                sendingMessage.createdAt,
                sendingMessage.file,
                UsedeskMessageClient.Status.SEND_FAILED
            )
            is UsedeskMessageClientAudio -> UsedeskMessageClientAudio(
                sendingMessage.id,
                sendingMessage.createdAt,
                sendingMessage.file,
                UsedeskMessageClient.Status.SEND_FAILED
            )
            else -> null
        }?.let(this@ChatInteractor::onMessageUpdate)
    }

    override fun loadForm(messageId: Long) {
        ioScope.launch {
            val form = eventMutex.withLock {
                if (!formLoadSet.contains(messageId)) {
                    formLoadSet.add(messageId)
                    modelFlow.value.messages
                        .asSequence()
                        .filterIsInstance<UsedeskMessageAgentText>()
                        .firstOrNull { it.id == messageId }
                        ?.forms
                        ?.filterIsInstance<Field.List>()
                } else null
            }
            if (form?.isNotEmpty() == true) {
                ioScope.launch {
                    while (true) {
                        val loadedForm = apiRepository.loadForm(
                            configuration,
                            form
                        )
                    }
                }
            }
        }
    }

    override fun send(agentMessage: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        ioScope.launch {
            apiRepository.send(agentMessage.id, feedback)

            onMessageUpdate(
                UsedeskMessageAgentText(
                    agentMessage.id,
                    agentMessage.createdAt,
                    agentMessage.text,
                    agentMessage.convertedText,
                    agentMessage.forms,
                    formsLoaded = agentMessage.formsLoaded,
                    feedbackNeeded = false,
                    feedback,
                    agentMessage.name,
                    agentMessage.avatar
                )
            )
        }
    }

    override fun send(offlineForm: UsedeskOfflineForm) {
        when {
            offlineFormToChat -> offlineForm.run {
                val fields = offlineForm.fields
                    .map(UsedeskOfflineForm.Field::value)
                    .filter(String::isNotEmpty)
                val strings =
                    listOf(clientName, clientEmail, topic) + fields + offlineForm.message
                initClientOfflineForm = strings.joinToString(separator = "\n")
                chatInited?.let(this@ChatInteractor::onChatInited)
            }
            else -> apiRepository.send(
                configuration,
                offlineForm
            )
        }
    }

    override fun sendAgain(id: Long) {
        val message = modelFlow.value.messages.firstOrNull { it.id == id }
        if (message is UsedeskMessageClient
            && message.status == UsedeskMessageClient.Status.SEND_FAILED
        ) {
            when (message) {
                is UsedeskMessageClientText -> {
                    val sendingMessage = UsedeskMessageClientText(
                        message.id,
                        message.createdAt,
                        message.text,
                        message.convertedText,
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
                else -> {}
            }
        }
    }

    override fun removeMessage(id: Long) {
        cachedMessages.getNotSentMessages()
            .firstOrNull { it.localId == id }
            ?.let {
                cachedMessages.removeNotSentMessage(it)
                onMessageRemove(it as UsedeskMessage)
            }
    }

    override fun setMessageDraft(messageDraft: UsedeskMessageDraft) {
        runBlocking {
            cachedMessages.setMessageDraft(messageDraft, true)
        }
    }

    override fun getMessageDraft() = runBlocking { cachedMessages.getMessageDraft() }

    override fun sendMessageDraft() {
        runBlocking {
            val messageDraft = cachedMessages.setMessageDraft(
                UsedeskMessageDraft(),
                false
            )

            send(messageDraft.text)
            send(messageDraft.files)
            //TODO: sync
        }
    }

    private fun createSendingMessage(text: String) = UsedeskMessageClientText(
        cachedMessages.getNextLocalId(),
        Calendar.getInstance(),
        text,
        apiRepository.convertText(text),
        UsedeskMessageClient.Status.SENDING
    )

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
            fileInfo.isImage() -> UsedeskMessageClientImage(
                localId,
                calendar,
                file,
                UsedeskMessageClient.Status.SENDING
            )
            fileInfo.isVideo() -> UsedeskMessageClientVideo(
                localId,
                calendar,
                file,
                UsedeskMessageClient.Status.SENDING
            )
            fileInfo.isAudio() -> UsedeskMessageClientAudio(
                localId,
                calendar,
                file,
                UsedeskMessageClient.Status.SENDING
            )
            else -> UsedeskMessageClientFile(
                localId,
                calendar,
                file,
                UsedeskMessageClient.Status.SENDING
            )
        }
    }

    override fun loadPreviousMessagesPage() = runBlocking {
        oldMutex.withLock {
            oldMessagesLoadDeferred ?: createPreviousMessagesJobLockedAsync()
        }?.await()
    } ?: true

    private fun createPreviousMessagesJobLockedAsync(): Deferred<Boolean>? {
        val messages = modelFlow.value.messages
        val oldestMessageId = messages.firstOrNull()?.id
        val token = token
        return when {
            oldestMessageId != null && token != null -> ioScope.async {
                val result = apiRepository.loadPreviousMessages(
                    configuration,
                    token,
                    oldestMessageId
                )
                val hasUnloadedMessages = result !is LoadPreviousMessageResult.Done ||
                        result.messages.isNotEmpty()
                oldMutex.withLock {
                    oldMessagesLoadDeferred = when {
                        hasUnloadedMessages -> null
                        else -> CompletableDeferred(false)
                    }
                }
                hasUnloadedMessages
            }.also {
                oldMessagesLoadDeferred = it
            }
            else -> null
        }
    }

    override fun release() {
        ioScope.cancel()
        disconnect()
    }

    private fun sendUserEmail() {
        try {
            token?.let {
                apiRepository.setClient(
                    configuration.copy(
                        clientToken = it
                    )
                )
            }
        } catch (e: UsedeskException) {
            actionListeners.onException(e)
        }
    }

    private fun onChatInited(chatInited: ChatInited) {
        this.token = chatInited.token
        if (configuration.clientToken != chatInited.token) {
            runBlocking {
                setModel {
                    copy(clientToken = chatInited.token)
                }
            }
        }

        if (chatInited.status in ACTIVE_STATUSES) {
            unlockFirstMessage()
        }

        val oldConfiguration = userInfoRepository.getConfiguration(configuration)

        if (initClientOfflineForm != null ||
            oldConfiguration?.clientInitMessage == initClientMessage
        ) {
            initClientMessage = null
        }

        userInfoRepository.setConfiguration(configuration.copy(clientToken = token))

        val model = modelFlow.value
        val ids = model.messages.map(UsedeskMessage::id)
        val filteredMessages = chatInited.messages.filter { it.id !in ids }
        val notSentMessages = cachedMessages.getNotSentMessages()
            .mapNotNull { it as? UsedeskMessage }
        val filteredNotSentMessages = notSentMessages.filter { it.id !in ids }
        val needToResendMessages = model.messages.isNotEmpty()
        onMessagesNew(
            new = filteredMessages + filteredNotSentMessages,
            isInited = true
        )

        when {
            chatInited.waitingEmail -> sendUserEmail()
            else -> eventListener.onSetEmailSuccess()
        }
        when {
            needToResendMessages -> {
                val initedNotSentIds = initedNotSentMessages.map(UsedeskMessage::id)
                notSentMessages.filter {
                    it.id !in initedNotSentIds
                }.forEach {
                    ioScope.launch {
                        sendAgain(it.id)
                    }
                }
            }
            else -> initedNotSentMessages = notSentMessages
        }
    }

    private fun CoroutineScope.launchSafe(
        onRun: () -> Unit,
        onThrowable: (Throwable) -> Unit = Throwable::printStackTrace
    ) = launch {
        try {
            onRun()
        } catch (e: Throwable) {
            onThrowable(e)
        }
    }

    companion object {
        private val ACTIVE_STATUSES = listOf(1, 5, 6, 8)
    }
}