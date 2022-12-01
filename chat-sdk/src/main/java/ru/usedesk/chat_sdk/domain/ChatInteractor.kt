package ru.usedesk.chat_sdk.domain

import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.*
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.domain.IUsedeskChat.*
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings.WorkType
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import java.util.*
import javax.inject.Inject

internal class ChatInteractor @Inject constructor(
    private val configuration: UsedeskChatConfiguration,
    private val userInfoRepository: IUserInfoRepository,
    private val apiRepository: IApiRepository,
    private val cachedMessagesRepository: ICachedMessagesRepository //Переместить в ap
) : IUsedeskChat {

    private var initClientMessage: String? = configuration.clientInitMessage
    private var initClientOfflineForm: String? = null

    private val modelFlow = MutableStateFlow(Model(clientToken = configuration.clientToken ?: ""))

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

    var oldModel: Model? = null
    private fun Model.onModelUpdated(oldModel: Model?, listener: IUsedeskActionListener) {
        if (oldModel?.connectionState != connectionState) {
            listener.onConnectionState(connectionState)
        }
        if (oldModel?.clientToken != clientToken && clientToken.isNotEmpty()) {
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
            setModel {
                copy(connectionState = UsedeskConnectionState.CONNECTED)
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
            modelLocked {
                if (clientToken.isNotEmpty()) {
                    ioScope.launch {
                        val response = apiRepository.sendInit(configuration, clientToken)
                    }
                }
            }
        }

        override fun onFeedback() {
            setModel {
                copy(feedbackEvent = UsedeskSingleLifeEvent(Unit))
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
            setModel {
                copy(offlineFormSettings = offlineFormSettings)
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

    override fun createChat(apiToken: String, onResult: (CreateChatResult) -> Unit) {
        ioScope.launch {
            val response = apiRepository.initChat(
                configuration,
                apiToken
            )
            val result = when (response) {
                is InitChatResponse.ApiError -> CreateChatResult.Error
                is InitChatResponse.Done -> {
                    userInfoRepository.setConfiguration(configuration.copy(clientToken = response.clientToken))
                    CreateChatResult.Done(response.clientToken)
                }
            }
            onResult(result)
        }
    }

    private fun launchConnect() {
        ioScope.launch {
            unlockFirstMessage()
            resetFirstMessageLock()

            val model = modelLocked()
            apiRepository.connect(
                configuration.urlChat,
                model.clientToken,
                configuration,
                eventListener
            )
        }
    }

    override fun connect() {
        setModel {
            copy(
                clientToken = configuration.clientToken
                    ?: userInfoRepository.getConfiguration(configuration)?.clientToken
                    ?: clientToken,
                connectionState = when (connectionState) {
                    UsedeskConnectionState.DISCONNECTED -> {
                        reconnectJob?.cancel()
                        launchConnect()
                        UsedeskConnectionState.RECONNECTING
                    }
                    else -> connectionState
                }
            )
        }
    }

    private fun setModel(onUpdate: Model.() -> Model): Model = runBlocking {
        eventMutex.withLock {
            modelFlow.value.onUpdate().also {
                modelFlow.value = it
            }
        }
    }

    private fun modelLocked(onModel: suspend Model.() -> Unit = {}): Model = runBlocking {
        eventMutex.withLock {
            modelFlow.value.apply {
                onModel()
            }
        }
    }

    private fun onMessageUpdate(message: UsedeskMessage) {
        setModel {
            copy(messages = messages.map {
                when {
                    it is UsedeskMessageOwner.Client &&
                            message is UsedeskMessageOwner.Client &&
                            it.localId == message.localId || it is UsedeskMessageOwner.Agent &&
                            message is UsedeskMessageOwner.Agent &&
                            it.id == message.id -> message
                    else -> it
                }
            })
        }
    }

    private fun onMessageRemove(message: UsedeskMessage) {
        setModel {
            copy(
                messages = messages.filter { it.id != message.id }
            )
        }
    }

    private fun onMessagesNew(
        old: List<UsedeskMessage> = listOf(),
        new: List<UsedeskMessage> = listOf(),
        isInited: Boolean
    ) {
        setModel {
            copy(
                messages = old + messages + new,
                inited = isInited
            )
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
        modelLocked {
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
                    ioScope.launch { sendText(sendingMessage) }
                }
            }
        }
    }

    private fun sendAdditionalFieldsIfNeededAsync() {
        modelLocked {
            if (additionalFieldsNeeded) {
                additionalFieldsNeeded = false
                if (configuration.additionalFields.isNotEmpty() ||
                    configuration.additionalNestedFields.isNotEmpty()
                ) {
                    ioScope.launch {
                        firstMessageLock?.withLock {}
                        delay(3000)
                        val response = apiRepository.sendFields(
                            clientToken,
                            configuration,
                            configuration.additionalFields,
                            configuration.additionalNestedFields
                        )
                        if (response is SendAdditionalFieldsResponse.Error) {
                            eventMutex.withLock {
                                additionalFieldsNeeded = true
                            }
                        }
                    }
                }
            }
        }
    }

    override fun send(usedeskFileInfoList: List<UsedeskFileInfo>) {
        ioScope.launch {
            val sendingMessages = usedeskFileInfoList.map(this@ChatInteractor::createSendingMessage)

            eventListener.onMessagesNewReceived(sendingMessages)

            eventMutex.withLock {
                sendingMessages.forEach(::sendFileAsync)
            }
        }
    }

    private fun sendFileAsync(fileMessage: UsedeskMessage.File) {
        ioScope.launch {
            cachedMessagesRepository.addNotSentMessage(fileMessage as UsedeskMessageOwner.Client)

            val uri = Uri.parse(fileMessage.file.content)
            val deferredCachedUri = cachedMessagesRepository.getCachedFileAsync(uri)
            val cachedUri = deferredCachedUri.await()
            val newFile = fileMessage.file.copy(content = cachedUri.toString())

            when (fileMessage) {
                is UsedeskMessageClientAudio -> fileMessage.copy(file = newFile)
                is UsedeskMessageClientVideo -> fileMessage.copy(file = newFile)
                is UsedeskMessageClientImage -> fileMessage.copy(file = newFile)
                is UsedeskMessageClientFile -> fileMessage.copy(file = newFile)
                else -> null
            }?.let { cachedNotSentMessage ->
                cachedMessagesRepository.updateNotSentMessage(cachedNotSentMessage)
                eventListener.onMessageUpdated(cachedNotSentMessage)
                firstMessageLock?.withLock {}
                val model = modelLocked()
                val response = apiRepository.sendFile(
                    configuration,
                    model.clientToken,
                    UsedeskFileInfo(
                        cachedUri,
                        cachedNotSentMessage.file.type,
                        cachedNotSentMessage.file.name
                    ),
                    cachedNotSentMessage.localId
                )
                when (response) {
                    is SendFileResponse.Done -> {
                        unlockFirstMessage()
                        cachedMessagesRepository.removeNotSentMessage(cachedNotSentMessage)
                        cachedMessagesRepository.removeFileFromCache(Uri.parse(fileMessage.file.content))
                        sendAdditionalFieldsIfNeededAsync()
                    }
                    is SendFileResponse.Error -> {
                        when (fileMessage) {
                            is UsedeskMessageClientFile -> fileMessage.copy(
                                status = UsedeskMessageOwner.Client.Status.SEND_FAILED
                            )
                            is UsedeskMessageClientImage -> fileMessage.copy(
                                status = UsedeskMessageOwner.Client.Status.SEND_FAILED
                            )
                            is UsedeskMessageClientVideo -> fileMessage.copy(
                                status = UsedeskMessageOwner.Client.Status.SEND_FAILED
                            )
                            is UsedeskMessageClientAudio -> fileMessage.copy(
                                status = UsedeskMessageOwner.Client.Status.SEND_FAILED
                            )
                            else -> null
                        }?.let(this@ChatInteractor::onMessageUpdate)
                    }
                }
            }
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
                val lock = firstMessageLock
                when (lock?.isLocked) {
                    false -> {
                        lock.lock()
                        null
                    }
                    true -> lock
                    else -> null
                }
            }?.withLock { }
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
                }
            }
        }
    }

    private fun sendText(cachedMessage: UsedeskMessageClientText) {
        cachedMessagesRepository.addNotSentMessage(cachedMessage)
        lockFirstMessage()
        when (apiRepository.sendText(cachedMessage)) {
            is SocketSendResponse.Done -> {
                cachedMessagesRepository.removeNotSentMessage(cachedMessage)
                sendAdditionalFieldsIfNeededAsync()
            }
            is SocketSendResponse.Error -> onMessageUpdate(
                cachedMessage.copy(status = UsedeskMessageOwner.Client.Status.SEND_FAILED)
            )
        }
        unlockFirstMessage()
    }

    override fun loadForm(messageId: Long) { //TODO:!!!
        /*ioScope.launch {
            val form = modelLocked {
                if (!formLoadSet.contains(messageId)) {
                    formLoadSet.add(messageId)
                    messages.asSequence()
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
        }*/
    }

    override fun send(agentMessage: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        ioScope.launch {
            val response = apiRepository.sendFeedback(agentMessage.id, feedback)
            when (response) {
                is SocketSendResponse.Done -> onMessageUpdate(
                    agentMessage.copy(
                        feedbackNeeded = false,
                        feedback = feedback
                    )
                )
                is SocketSendResponse.Error -> {
                    delay(3000)
                    send(agentMessage, feedback)
                }
            }
        }
    }

    override fun send(
        offlineForm: UsedeskOfflineForm,
        onResult: (SendOfflineFormResult) -> Unit
    ) {
        val result = when {
            offlineFormToChat -> offlineForm.run {
                val fields = offlineForm.fields
                    .map(UsedeskOfflineForm.Field::value)
                    .filter(String::isNotEmpty)
                val strings = listOf(clientName, clientEmail, topic) + fields + offlineForm.message
                initClientOfflineForm = strings.joinToString(separator = "\n")
                chatInited?.let(this@ChatInteractor::onChatInited)
                SendOfflineFormResult.Done
            }
            else -> {
                val response = apiRepository.sendOfflineForm(
                    configuration,
                    offlineForm
                )
                when (response) {
                    SendOfflineFormResponse.Done -> SendOfflineFormResult.Done
                    is SendOfflineFormResponse.Error -> SendOfflineFormResult.Error
                }
            }
        }
        onResult(result)
    }

    override fun sendAgain(messageId: Long) {
        modelLocked {
            val message = messages.firstOrNull { it.id == messageId }
            if (message is UsedeskMessageOwner.Client
                && message.status == UsedeskMessageOwner.Client.Status.SEND_FAILED
            ) {
                ioScope.launch {
                    when (message) {
                        is UsedeskMessageClientText -> message.copy(
                            status = UsedeskMessageOwner.Client.Status.SENDING
                        )
                        is UsedeskMessageClientImage -> message.copy(
                            status = UsedeskMessageOwner.Client.Status.SENDING
                        )
                        is UsedeskMessageClientVideo -> message.copy(
                            status = UsedeskMessageOwner.Client.Status.SENDING
                        )
                        is UsedeskMessageClientAudio -> message.copy(
                            status = UsedeskMessageOwner.Client.Status.SENDING
                        )
                        is UsedeskMessageClientFile -> message.copy(
                            status = UsedeskMessageOwner.Client.Status.SENDING
                        )
                        else -> null
                    }?.let { sendingMessage ->
                        onMessageUpdate(sendingMessage)
                        when (sendingMessage) {
                            is UsedeskMessage.File -> sendFileAsync(sendingMessage)
                            is UsedeskMessageClientText -> sendText(sendingMessage)
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun removeMessage(messageId: Long) {
        cachedMessagesRepository.getNotSentMessages()
            .firstOrNull { it.localId == messageId }
            ?.let {
                cachedMessagesRepository.removeNotSentMessage(it)
                onMessageRemove(it as UsedeskMessage)
            }
    }

    override fun setMessageDraft(messageDraft: UsedeskMessageDraft) {
        runBlocking {
            cachedMessagesRepository.setMessageDraft(messageDraft, true)
        }
    }

    override fun getMessageDraft() = runBlocking { cachedMessagesRepository.getMessageDraft() }

    override fun sendMessageDraft() {
        val messageDraft = runBlocking {
            cachedMessagesRepository.setMessageDraft(
                UsedeskMessageDraft(),
                false
            )
        }

        send(messageDraft.text)
        send(messageDraft.files)
    }

    private fun createSendingMessage(text: String) = UsedeskMessageClientText(
        cachedMessagesRepository.getNextLocalId(),
        Calendar.getInstance(),
        text,
        apiRepository.convertText(text),
        UsedeskMessageOwner.Client.Status.SENDING
    )

    private fun createSendingMessage(fileInfo: UsedeskFileInfo): UsedeskMessage.File {
        val localId = cachedMessagesRepository.getNextLocalId()
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
                UsedeskMessageOwner.Client.Status.SENDING
            )
            fileInfo.isVideo() -> UsedeskMessageClientVideo(
                localId,
                calendar,
                file,
                UsedeskMessageOwner.Client.Status.SENDING
            )
            fileInfo.isAudio() -> UsedeskMessageClientAudio(
                localId,
                calendar,
                file,
                UsedeskMessageOwner.Client.Status.SENDING
            )
            else -> UsedeskMessageClientFile(
                localId,
                calendar,
                file,
                UsedeskMessageOwner.Client.Status.SENDING
            )
        }
    }

    override fun loadPreviousMessagesPage() {
        setModel {
            val oldestMessageId = messages.firstOrNull()?.id
            copy(
                previousPageIsLoading = when {
                    !previousPageIsLoading && previousPageIsAvailable && oldestMessageId != null -> {
                        ioScope.launch {
                            //TODO: инит приходит позже чем запрос на загрузку страницы, поэтому ничего и не запускается
                            val response = apiRepository.loadPreviousMessages(
                                configuration,
                                clientToken,
                                oldestMessageId
                            )
                            setModel {
                                copy(
                                    previousPageIsLoading = false,
                                    previousPageIsAvailable =
                                    response !is LoadPreviousMessageResponse.Done ||
                                            response.messages.isNotEmpty()
                                )
                            }
                        }
                        true
                    }
                    else -> previousPageIsLoading
                }
            )
        }
    }

    override fun release() {
        ioScope.cancel()
        disconnect()
    }

    private fun onChatInited(chatInited: ChatInited) {
        setModel {
            copy(clientToken = chatInited.token)
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

        val model = modelLocked()

        userInfoRepository.setConfiguration(configuration.copy(clientToken = model.clientToken))

        val ids = model.messages.map(UsedeskMessage::id)
        val filteredMessages = chatInited.messages.filter { it.id !in ids }
        val notSentMessages = cachedMessagesRepository.getNotSentMessages()
            .mapNotNull { it as? UsedeskMessage }
        val filteredNotSentMessages = notSentMessages.filter { it.id !in ids }
        val needToResendMessages = model.messages.isNotEmpty()
        onMessagesNew(
            new = filteredMessages + filteredNotSentMessages,
            isInited = true
        )

        when {
            chatInited.waitingEmail -> model.clientToken.let {
                apiRepository.setClient(
                    configuration.copy(clientToken = it)
                )
            }
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

    companion object {
        private val ACTIVE_STATUSES = listOf(1, 5, 6, 8)
    }
}