package ru.usedesk.chat_sdk.domain

import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.*
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.data.repository.messages.ICachedMessagesRepository
import ru.usedesk.chat_sdk.di.IRelease
import ru.usedesk.chat_sdk.domain.IUsedeskChat.Model
import ru.usedesk.chat_sdk.domain.IUsedeskChat.SendOfflineFormResult
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings.WorkType
import ru.usedesk.common_sdk.entity.UsedeskSingleLifeEvent
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil
import java.util.*
import javax.inject.Inject

internal class ChatInteractor @Inject constructor(
    private val initConfiguration: UsedeskChatConfiguration,
    private val userInfoRepository: IUserInfoRepository,
    private val apiRepository: IApiRepository,
    private val cachedMessagesRepository: ICachedMessagesRepository
) : IUsedeskChat, IRelease {

    private var initClientMessage: String? = initConfiguration.clientInitMessage
    private var initClientOfflineForm: String? = null

    private val modelFlow: MutableStateFlow<Model>

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

    private val eventListener = object : IApiRepository.EventListener {
        override fun onConnected() {
            setModel { copy(connectionState = UsedeskConnectionState.CONNECTED) }
        }

        override fun onDisconnected() {
            runBlocking {
                reconnectJob?.cancel()
                reconnectJob = ioScope.launch {
                    delay(REPEAT_DELAY)
                    connect()
                }
                setModel { copy(connectionState = UsedeskConnectionState.DISCONNECTED) }
            }
        }

        override fun onTokenError() {
            modelLocked {
                if (clientToken.isNotEmpty()) {
                    ioScope.launch {
                        val response = apiRepository.sendInit(initConfiguration, clientToken)
                    }
                }
            }
        }

        override fun onFeedback() {
            setModel { copy(feedbackEvent = UsedeskSingleLifeEvent(Unit)) }
        }

        override fun onException(exception: Exception) {
            actionListeners.onException(exception)
        }

        override fun onChatInited(chatInited: ChatInited) {
            this@ChatInteractor.chatInited = chatInited
            this@ChatInteractor.onChatInited(chatInited)
        }

        override fun onMessagesOldReceived(
            messages: List<UsedeskMessage>,
            forms: List<UsedeskForm>
        ) {
            this@ChatInteractor.onMessagesNew(
                old = messages,
                isInited = false,
                forms = forms
            )
        }

        override fun onMessagesNewReceived(
            messages: List<UsedeskMessage>,
            forms: List<UsedeskForm>
        ) {
            this@ChatInteractor.onMessagesNew(
                new = messages,
                isInited = false,
                forms = forms
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

    private fun Model.onModelUpdated(oldModel: Model?, listener: IUsedeskActionListener) {
        when (oldModel?.messages) {
            messages -> {
                listener.onModel(
                    this,
                    listOf(),
                    listOf(),
                    listOf()
                )
            }
            else -> {
                val newMessages = mutableListOf<UsedeskMessage>()
                val updatedMessages = mutableListOf<UsedeskMessage>()
                val removedMessages = mutableListOf<UsedeskMessage>()
                messages.forEach { message ->
                    val oldMessage = oldModel?.messages?.firstOrNull { it.id == message.id }
                    when {
                        oldMessage == null -> {
                            if (!inited) {
                                newMessages.add(message)
                            }
                        }
                        oldMessage != message -> updatedMessages.add(message)
                    }
                }
                oldModel?.messages?.forEach { oldMessage ->
                    if (messages.all { message -> message.id != oldMessage.id }) {
                        removedMessages.add(oldMessage)
                    }
                }
                listener.onModel(
                    this,
                    newMessages,
                    updatedMessages,
                    removedMessages
                )
            }
        }
    }

    init {
        val oldConfiguration = userInfoRepository.getConfiguration()

        initClientMessage = when {
            initClientOfflineForm != null ||
                    oldConfiguration?.clientInitMessage == initClientMessage -> null
            else -> initConfiguration.clientInitMessage
        }

        modelFlow = MutableStateFlow(
            Model(
                clientToken = initConfiguration.clientToken
                    ?: oldConfiguration?.clientToken
                    ?: ""
            )
        )

        ioScope.launch {
            modelFlow.collect { model ->
                model.onModelUpdated(oldModel, actionListeners)
                oldModel = model
            }
        }

        launchConnect()
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

    private fun launchConnect() {
        ioScope.launch {
            unlockFirstMessage()
            resetFirstMessageLock()

            val model = modelLocked()
            apiRepository.connect(
                initConfiguration.urlChat,
                model.clientToken,
                initConfiguration,
                eventListener
            )
        }
    }

    private fun connect() {
        setModel {
            when (connectionState) {
                UsedeskConnectionState.DISCONNECTED -> {
                    reconnectJob?.cancel()
                    launchConnect()
                    copy(
                        clientToken = initConfiguration.clientToken
                            ?: userInfoRepository.getConfiguration()?.clientToken
                            ?: clientToken,
                        connectionState = UsedeskConnectionState.RECONNECTING
                    )
                }
                else -> this
            }
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
        forms: List<UsedeskForm> = listOf(),
        isInited: Boolean
    ) {
        setModel {
            copy(
                messages = old + messages + new,
                formMap = formMap.toMutableMap().apply {
                    forms.forEach { put(it.id, it) }
                },
                inited = isInited
            )
        }
    }

    private fun disconnect() {
        reconnectJob?.cancel()
        CoroutineScope(Dispatchers.IO).launch {
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
            eventListener.onMessagesNewReceived(listOf(sendingMessage), listOf())

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
                if (initConfiguration.additionalFields.isNotEmpty() ||
                    initConfiguration.additionalNestedFields.isNotEmpty()
                ) {
                    ioScope.launch {
                        firstMessageLock?.withLock {}
                        delay(REPEAT_DELAY)
                        val response = apiRepository.sendFields(
                            clientToken,
                            initConfiguration,
                            initConfiguration.additionalFields,
                            initConfiguration.additionalNestedFields
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

            eventListener.onMessagesNewReceived(sendingMessages, listOf())

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
                    initConfiguration,
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
                    true -> lock
                    false -> {
                        lock.lock(this@ChatInteractor)
                        null
                    }
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

    override fun loadForm(messageId: Long) {
        setModel {
            val form = formMap[messageId]
            when (form?.state) {
                UsedeskForm.State.NOT_LOADED -> copy(
                    formMap = formMap.toMutableMap().apply {
                        put(
                            messageId,
                            form.copy(state = UsedeskForm.State.LOADING).apply {
                                launchLoadForm(form)
                            }
                        )
                    }
                )
                else -> this
            }
        }
    }

    private fun launchLoadForm(form: UsedeskForm) {
        ioScope.launch {
            val response = apiRepository.loadForm(
                initConfiguration,
                form
            )
            when (response) {
                is LoadFormResponse.Done -> setModel {
                    copy(
                        formMap = formMap.toMutableMap().apply {
                            put(
                                form.id,
                                response.form
                            )
                        }
                    )
                }
                is LoadFormResponse.Error -> {
                    delay(REPEAT_DELAY)
                    launchLoadForm(form)
                }
            }
        }
    }

    override fun saveForm(form: UsedeskForm) {
        setModel {
            copy(
                formMap = formMap.toMutableMap().apply {
                    put(form.id, form)
                    ioScope.launch {
                        //TODO:
                    }
                }
            )
        }
    }

    override fun send(form: UsedeskForm) {
        setModel {
            when (formMap[form.id]?.state) {
                UsedeskForm.State.SENDING -> this
                else -> {
                    val newFields = form.fields.map { field ->
                        when (field) {
                            is UsedeskMessageAgentText.Field.CheckBox -> field.copy(
                                hasError = field.required && !field.checked
                            )
                            is UsedeskMessageAgentText.Field.List -> {
                                val tree = field.tree ?: listOf(field)
                                val isValid = tree.all { !it.required || field.selected != null }
                                field.copy(hasError = !isValid)
                            }
                            is UsedeskMessageAgentText.Field.Text -> {
                                val text = field.text
                                val isValid = when (field.type) {
                                    UsedeskMessageAgentText.Field.Text.Type.EMAIL -> when {
                                        field.required -> UsedeskValidatorUtil.isValidEmailNecessary(
                                            text
                                        )
                                        else -> UsedeskValidatorUtil.isValidEmail(text)
                                    }
                                    UsedeskMessageAgentText.Field.Text.Type.PHONE -> when {
                                        field.required -> UsedeskValidatorUtil.isValidPhoneNecessary(
                                            text
                                        )
                                        else -> UsedeskValidatorUtil.isValidPhone(text)
                                    }
                                    else -> !field.required || text.any { it.isLetter() }
                                }
                                field.copy(hasError = !isValid)
                            }
                        }
                    }
                    val anyHasError = newFields.any { it.hasError }
                    copy(
                        formMap = formMap.toMutableMap().apply {
                            val newForm = when {
                                anyHasError -> form.copy(
                                    fields = newFields,
                                    state = UsedeskForm.State.LOADED
                                )
                                else -> form.copy(
                                    fields = newFields,
                                    state = UsedeskForm.State.SENDING
                                ).apply {
                                    launchSendForm(this)
                                }
                            }
                            put(form.id, newForm)
                        }
                    )
                }
            }
        }
    }

    private fun launchSendForm(form: UsedeskForm) {
        //TODO
    }

    override fun send(agentMessage: UsedeskMessageAgentText, feedback: UsedeskFeedback) {
        ioScope.launch {
            when (apiRepository.sendFeedback(agentMessage.id, feedback)) {
                is SocketSendResponse.Done -> onMessageUpdate(
                    agentMessage.copy(
                        feedbackNeeded = false,
                        feedback = feedback
                    )
                )
                is SocketSendResponse.Error -> {
                    delay(REPEAT_DELAY)
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
                    initConfiguration,
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
            when (val oldestMessageId = messages.firstOrNull()?.id) {
                null -> this
                else -> copy(
                    previousPageIsLoading = when {
                        !previousPageIsLoading && previousPageIsAvailable -> {
                            ioScope.launch {
                                val response = apiRepository.loadPreviousMessages(
                                    initConfiguration,
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
    }

    override fun release() {
        ioScope.cancel()
        disconnect()
    }

    private fun onChatInited(chatInited: ChatInited) {
        val model = setModel {
            copy(
                clientToken = chatInited.token,
                formMap = chatInited.forms.associateBy { it.id }
            )
        }
        userInfoRepository.updateConfiguration { copy(clientToken = chatInited.token) }

        if (chatInited.status in ACTIVE_STATUSES) {
            unlockFirstMessage()
        }

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
                    initConfiguration.copy(clientToken = it)
                )
            }
            else -> eventListener.onSetEmailSuccess()
        }
        when {
            needToResendMessages -> {
                val initedNotSentIds = initedNotSentMessages.map(UsedeskMessage::id)
                notSentMessages
                    .filter { it.id !in initedNotSentIds }
                    .forEach {
                        ioScope.launch {
                            sendAgain(it.id)
                        }
                    }
            }
            else -> initedNotSentMessages = notSentMessages
        }
    }

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

        override fun onModel(
            model: Model,
            newMessages: List<UsedeskMessage>,
            updatedMessages: List<UsedeskMessage>,
            removedMessages: List<UsedeskMessage>
        ) {
            listeners.forEach {
                it.onModel(
                    model,
                    newMessages,
                    updatedMessages,
                    removedMessages
                )
            }
        }

        override fun onException(usedeskException: Exception) {
            listeners.forEach { it.onException(usedeskException) }
        }
    }

    companion object {
        private val ACTIVE_STATUSES = listOf(1, 5, 6, 8)
        private const val REPEAT_DELAY = 5000L
    }
}