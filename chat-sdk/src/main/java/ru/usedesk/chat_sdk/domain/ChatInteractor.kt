
package ru.usedesk.chat_sdk.domain

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.LoadPreviousMessageResponse
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.SendAdditionalFieldsResponse
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.SendFileResponse
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.SendOfflineFormResponse
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.SocketSendResponse
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.data.repository.form.IFormRepository
import ru.usedesk.chat_sdk.data.repository.form.IFormRepository.LoadFormResponse
import ru.usedesk.chat_sdk.data.repository.form.IFormRepository.SendFormResponse
import ru.usedesk.chat_sdk.data.repository.messages.ICachedMessagesRepository
import ru.usedesk.chat_sdk.data.repository.thumbnail.IThumbnailRepository
import ru.usedesk.chat_sdk.di.IRelease
import ru.usedesk.chat_sdk.domain.IUsedeskChat.IFileUploadProgressListener
import ru.usedesk.chat_sdk.domain.IUsedeskChat.Model
import ru.usedesk.chat_sdk.domain.IUsedeskChat.SendOfflineFormResult
import ru.usedesk.chat_sdk.entity.ChatInited
import ru.usedesk.chat_sdk.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskConnectionState
import ru.usedesk.chat_sdk.entity.UsedeskFeedback
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.entity.UsedeskForm
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientAudio
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientFile
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientImage
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientText
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientVideo
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner
import ru.usedesk.chat_sdk.entity.UsedeskOfflineForm
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings
import ru.usedesk.chat_sdk.entity.UsedeskOfflineFormSettings.WorkType
import ru.usedesk.common_sdk.entity.UsedeskEvent
import java.util.Calendar
import javax.inject.Inject

internal class ChatInteractor @Inject constructor(
    private val initConfiguration: UsedeskChatConfiguration,
    private val userInfoRepository: IUserInfoRepository,
    private val apiRepository: IApiRepository,
    private val cachedMessagesRepository: ICachedMessagesRepository,
    private val formRepository: IFormRepository,
    private val thumbnailRepository: IThumbnailRepository
) : IUsedeskChat, IRelease {

    private var initClientMessage: String? = initConfiguration.clientInitMessage
    private var initClientOfflineForm: String? = null

    private val modelFlow: MutableStateFlow<Model>

    private val actionListeners = ActionListeners()

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var reconnectJob: Job? = null
    private val eventMutex = Mutex()
    private val firstMessageMutex = Mutex()
    private var firstMessageLock: Mutex? = null

    private var chatInited: ChatInited? = null
    private var offlineFormToChat = false

    private var initedNotSentMessages = listOf<UsedeskMessage>()

    private var additionalFieldsNeeded: Boolean = true
    private var previousMessagesLoadingJob: Job? = null

    private val fileUploadProgressListeners =
        mutableMapOf<Long, MutableSet<IFileUploadProgressListener>>()
    private val fileUploadProgressMutex = Mutex()

    private sealed interface Send {
        val localId: Long?

        class Text(
            val text: String,
            override val localId: Long?
        ) : Send

        class File(
            val file: UsedeskFileInfo,
            override val localId: Long?
        ) : Send
    }

    private val sendQueue = Queue<Send>(ioScope) {
        when (it) {
            is Send.File -> prepareToSendFile(it.file, it.localId)
            is Send.Text -> prepareToSendText(it.text, it.localId)
        }
    }
    private val sendTextQueue = Queue(ioScope, ::doSendText)
    private val sendFileQueue = Queue(ioScope, ::doSendFile)

    private val eventListener = object : IApiRepository.EventListener {
        override fun onConnected() {
            setModel { copy(connectionState = UsedeskConnectionState.CONNECTED) }
        }

        override fun onDisconnected() {
            runBlocking {
                reconnectJob?.cancel()
                previousMessagesLoadingJob?.cancel()
                reconnectJob = ioScope.launch {
                    delay(REPEAT_DELAY)
                    connect()
                }
                setModel {
                    previousMessagesLoadingJob = null
                    copy(connectionState = UsedeskConnectionState.DISCONNECTED)
                }
            }
        }

        override fun onTokenError() {
            val model = modelLocked()
            if (model.clientToken.isNotEmpty()) {
                ioScope.launch {
                    val response = apiRepository.sendInit(initConfiguration, model.clientToken)
                }
            }
        }

        override fun onFeedback() {
            setModel { copy(feedbackEvent = UsedeskEvent(Unit)) }
        }

        override fun onException(exception: Exception) {
            actionListeners.onException(exception)
        }

        override fun onChatInited(chatInited: ChatInited) {
            this@ChatInteractor.chatInited = chatInited
            ioScope.launch {
                this@ChatInteractor.onChatInited(chatInited)
            }
        }

        override fun onMessagesOldReceived(messages: List<UsedeskMessage>) {
            this@ChatInteractor.onMessagesNew(old = messages)
        }

        override fun onMessagesNewReceived(messages: List<UsedeskMessage>) {
            this@ChatInteractor.onMessagesNew(new = messages)
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
            var oldModel: Model? = null
            modelFlow.collect { model ->
                actionListeners.onModelUpdated(oldModel, model)
                oldModel = model
            }
        }

        ioScope.launch {
            thumbnailRepository.thumbnailMapFlow.collect {
                setModel { copy(thumbnailMap = it) }
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
            firstMessageMutex.withLock {
                firstMessageLock?.unlockSafe()
                firstMessageLock = Mutex()
            }

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
                    copy(connectionState = UsedeskConnectionState.RECONNECTING)
                }
                else -> this
            }
        }
    }

    private fun setModel(onUpdate: Model.() -> Model) = modelFlow.updateAndGet { it.onUpdate() }

    private fun modelLocked(onModel: Model.() -> Unit = {}): Model = runBlocking {
        eventMutex.withLock {
            modelFlow.value.apply { onModel() }
        }
    }

    private fun onMessageUpdate(message: UsedeskMessage) {
        if (message is UsedeskMessage.File && message.file.isVideo()) {
            thumbnailRepository.loadThumbnail(message)
        }
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
        setModel { copy(messages = messages.filter { it.id != message.id }) }
    }

    private fun onMessagesNew(
        old: List<UsedeskMessage> = listOf(),
        new: List<UsedeskMessage> = listOf()
    ) {
        (old + new).forEach {
            if (it is UsedeskMessage.File && it.file.isVideo()) {
                thumbnailRepository.loadThumbnail(it)
            }
        }
        setModel {
            copy(
                messages = old + messages + new,
                inited = true
            )
        }
    }

    override fun addActionListener(listener: IUsedeskActionListener) {
        actionListeners.add(listener)
        listener.onModelUpdated(null, modelLocked())
    }

    override fun removeActionListener(listener: IUsedeskActionListener) {
        actionListeners.remove(listener)
    }

    override fun isNoListeners(): Boolean = actionListeners.isNoListeners()

    override fun send(
        textMessage: String,
        localId: Long?
    ) {
        runBlocking {
            sendQueue.emit(Send.Text(textMessage, localId))
        }
    }

    private suspend fun prepareToSendText(
        textMessage: String,
        localId: Long?
    ) {
        val message = textMessage.trim()
        if (message.isNotEmpty()) {
            val sendingMessage = createSendingMessage(message, getNextLocalId(localId))
            onMessagesNew(new = listOf(sendingMessage))
            sendText(sendingMessage)
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

    override fun send(
        fileInfo: UsedeskFileInfo,
        localId: Long?
    ) {
        runBlocking {
            sendQueue.emit(Send.File(fileInfo, localId))
        }
    }

    override fun addFileUploadProgressListener(
        localMessageId: Long,
        listener: IFileUploadProgressListener
    ) {
        runBlocking {
            fileUploadProgressMutex.withLock {
                fileUploadProgressListeners.getOrPut(localMessageId) { mutableSetOf() }.run {
                    add(listener)
                }
            }
        }
    }

    override fun removeFileUploadProgressListener(
        localMessageId: Long,
        listener: IFileUploadProgressListener
    ) {
        runBlocking {
            fileUploadProgressMutex.withLock {
                fileUploadProgressListeners[localMessageId]?.run {
                    remove(listener)
                }
            }
        }
    }

    override fun send(fileInfoList: Collection<UsedeskFileInfo>) {
        fileInfoList.forEach { send(it) }
    }

    private suspend fun prepareToSendFile(
        fileInfo: UsedeskFileInfo,
        localId: Long?
    ) {
        val sendingMessage = cachedMessagesRepository.createSendingMessage(
            fileInfo,
            getNextLocalId(localId)
        )
        eventListener.onMessagesNewReceived(listOf(sendingMessage))
        sendFileQueue.emit(sendingMessage)
    }

    private suspend fun sendFileAgain(fileMessage: UsedeskMessage.File) {
        fileMessage as UsedeskMessageOwner.Client
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
            sendFileQueue.emit(cachedNotSentMessage)
        }
    }

    private suspend fun doSendFile(fileMessage: UsedeskMessage.File) {
        fileMessage as UsedeskMessageOwner.Client
        withFirstMessageLock {
            val model = modelLocked()
            val progressFlow = MutableStateFlow(0L to 0L)
            val progressJob = CoroutineScope(ioScope.coroutineContext + Job()).launch {
                progressFlow.collect { progress ->
                    if (progress.second != 0L) {
                        fileUploadProgressMutex.withLock {
                            fileUploadProgressListeners[fileMessage.localId]?.forEach {
                                it.onProgress(progress.first, progress.second)
                            }
                        }
                        if (progress.first == progress.second) {
                            cancel()
                        }
                    }
                }
            }
            val response = apiRepository.sendFile(
                initConfiguration,
                model.clientToken,
                UsedeskFileInfo(
                    fileMessage.file.content.toUri(),
                    fileMessage.file.type,
                    fileMessage.file.name
                ),
                fileMessage.localId,
                progressFlow
            )
            when (response) {
                is SendFileResponse.Done -> {
                    cachedMessagesRepository.removeNotSentMessage(fileMessage.localId)
                    cachedMessagesRepository.removeFileFromCache(Uri.parse(fileMessage.file.content))
                    sendAdditionalFieldsIfNeededAsync()
                    true
                }
                is SendFileResponse.Error -> {
                    progressJob.cancel()
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
                    false
                }
            }
        }
    }

    private suspend fun withFirstMessageLock(task: suspend () -> Boolean) {
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
        val success = task()
        firstMessageMutex.withLock {
            firstMessageLock?.apply {
                if (success) {
                    if (isLocked) {
                        delay(1000)
                    }
                    firstMessageLock = null
                }
                unlockSafe(this@ChatInteractor)
            }
        }
    }

    private suspend fun sendText(cachedMessage: UsedeskMessageClientText) {
        cachedMessagesRepository.addNotSentMessage(cachedMessage)
        sendTextQueue.emit(cachedMessage)
    }

    private suspend fun doSendText(textMessage: UsedeskMessageClientText) {
        withFirstMessageLock {
            when (apiRepository.sendText(textMessage)) {
                is SocketSendResponse.Done -> {
                    cachedMessagesRepository.removeNotSentMessage(textMessage.localId)
                    sendAdditionalFieldsIfNeededAsync()
                    true
                }
                is SocketSendResponse.Error -> {
                    onMessageUpdate(
                        textMessage.copy(status = UsedeskMessageOwner.Client.Status.SEND_FAILED)
                    )
                    false
                }
            }
        }
    }

    override fun loadForm(messageId: Long) {
        setModel {
            val form = formMap[messageId] ?: UsedeskForm(messageId)
            val message = when (form.state) {
                UsedeskForm.State.LOADING_FAILED,
                UsedeskForm.State.NOT_LOADED -> messages.asSequence()
                    .filterIsInstance<UsedeskMessageAgentText>()
                    .firstOrNull { it.id == messageId }
                else -> null
            }
            val newForm = when (message) {
                null -> form
                else -> form.copy(state = UsedeskForm.State.LOADING).apply {
                    launchLoadForm(
                        clientToken,
                        message
                    )
                }
            }
            copy(
                formMap = formMap.toMutableMap().apply {
                    put(
                        messageId,
                        newForm
                    )
                }
            )
        }
    }

    private fun launchLoadForm(
        clientToken: String,
        message: UsedeskMessageAgentText
    ) {
        ioScope.launch {
            val response = formRepository.loadForm(
                initConfiguration.urlChatApi,
                clientToken,
                message.id,
                message.fieldsInfo
            )
            setModel {
                val form = formMap[message.id] ?: UsedeskForm(message.id)
                copy(
                    formMap = formMap.toMutableMap().apply {
                        put(
                            message.id,
                            when (response) {
                                is LoadFormResponse.Done -> response.form
                                is LoadFormResponse.Error -> form.copy(state = UsedeskForm.State.LOADING_FAILED)
                            }
                        )
                    }
                )
            }
        }
    }

    override fun saveForm(form: UsedeskForm) {
        setModel {
            copy(
                formMap = formMap.toMutableMap().apply {
                    put(form.id, form)
                    ioScope.launch {
                        formRepository.saveForm(form)
                    }
                }
            )
        }
    }

    override fun send(form: UsedeskForm) {
        setModel {
            when (formMap[form.id]?.state) {
                UsedeskForm.State.NOT_LOADED,
                UsedeskForm.State.LOADING_FAILED,
                UsedeskForm.State.SENDING -> this
                else -> {
                    val validatedForm = formRepository.validateForm(form)
                    val hasError = validatedForm.fields.any { it.hasError }
                            || validatedForm.fields.all {
                        when (it) {
                            is UsedeskForm.Field.CheckBox -> !it.checked
                            is UsedeskForm.Field.List -> it.selected == null
                            is UsedeskForm.Field.Text -> it.text.isEmpty()
                        }
                    }
                    if (!hasError) {
                        launchSendForm(clientToken, validatedForm)
                    }
                    copy(
                        formMap = formMap.toMutableMap().apply {
                            val newForm = validatedForm.copy(
                                state = when {
                                    hasError -> UsedeskForm.State.LOADED
                                    else -> UsedeskForm.State.SENDING
                                }
                            )
                            put(
                                form.id,
                                newForm
                            )
                        }
                    )
                }
            }
        }
    }

    /*override fun addFileProgressListener(
        localMessageId: Long,
        onProgress: (Int) -> Unit
    ) {

    }*/

    private fun launchSendForm(
        clientToken: String,
        form: UsedeskForm
    ) {
        ioScope.launch {
            val response = formRepository.sendForm(
                initConfiguration.urlChatApi,
                clientToken,
                form
            )
            val newForm = when (response) {
                is SendFormResponse.Done -> response.form
                is SendFormResponse.Error -> form.copy(state = UsedeskForm.State.SENDING_FAILED)
            }
            setModel {
                copy(
                    formMap = formMap.toMutableMap().apply {
                        put(form.id, newForm)
                    }
                )
            }
        }
    }

    override fun send(
        agentMessage: UsedeskMessageAgentText,
        feedback: UsedeskFeedback
    ) {
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
        ioScope.launch {
            val result = when {
                offlineFormToChat -> offlineForm.run {
                    val fields = offlineForm.fields
                        .map(UsedeskOfflineForm.Field::value)
                        .filter(String::isNotEmpty)
                    val strings =
                        listOf(clientName, clientEmail, topic) + fields + offlineForm.message
                    initClientOfflineForm = strings.joinToString(separator = "\n")
                    chatInited?.let { onChatInited(it) }
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
    }

    override fun sendAgain(messageId: Long) {
        ioScope.launch {
            val model = modelLocked()
            val message = model.messages.firstOrNull { it.id == messageId }
            if (message is UsedeskMessageOwner.Client
                && message.status == UsedeskMessageOwner.Client.Status.SEND_FAILED
            ) {
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
                        is UsedeskMessage.File -> sendFileAgain(sendingMessage)
                        is UsedeskMessageClientText -> sendText(sendingMessage)
                        else -> {}
                    }
                }
            }
        }
    }

    override fun removeMessage(messageId: Long) {
        ioScope.launch {
            cachedMessagesRepository.getNotSentMessages()
                .firstOrNull { it.localId == messageId }
                ?.let {
                    cachedMessagesRepository.removeNotSentMessage(it.localId)
                    onMessageRemove(it as UsedeskMessage)
                }
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
            cachedMessagesRepository.getMessageDraft()
        }
        ioScope.launch {
            cachedMessagesRepository.setMessageDraft(
                UsedeskMessageDraft(),
                false
            )
        }

        send(messageDraft.text, null)
        messageDraft.files.forEach { file ->
            send(file, null)
        }
    }

    private suspend fun getNextLocalId(localId: Long?) =
        localId ?: cachedMessagesRepository.getNextLocalId()

    private fun createSendingMessage(
        text: String,
        localId: Long
    ) = UsedeskMessageClientText(
        localId,
        Calendar.getInstance(),
        text,
        apiRepository.convertText(text),
        UsedeskMessageOwner.Client.Status.SENDING
    )

    private fun launchPreviousMessagesLoading(
        oldestMessageId: Long,
        clientToken: String,
    ) = ioScope.launch {
        while (true) {
            val response = apiRepository.loadPreviousMessages(
                initConfiguration,
                clientToken,
                oldestMessageId
            )
            when (response) {
                is LoadPreviousMessageResponse.Done -> {
                    setModel {
                        previousMessagesLoadingJob = null
                        copy(
                            previousPageIsLoading = false,
                            previousPageIsAvailable = response.messages.isNotEmpty()
                        )
                    }
                    break
                }
                is LoadPreviousMessageResponse.Error -> delay(5000)
            }
        }
    }

    override fun loadPreviousMessagesPage() {
        setModel {
            when (val oldestMessageId = messages.firstOrNull()?.id) {
                null -> this
                else -> copy(
                    previousPageIsLoading = when {
                        !previousPageIsLoading && previousPageIsAvailable -> {
                            previousMessagesLoadingJob?.cancel()
                            previousMessagesLoadingJob = launchPreviousMessagesLoading(
                                oldestMessageId,
                                clientToken
                            )
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
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            apiRepository.disconnect()
        }
    }

    private fun Mutex.unlockSafe(owner: Any? = null) {
        if (isLocked) {
            unlock(owner)
        }
    }

    private suspend fun onChatInited(chatInited: ChatInited) {
        val model = setModel { copy(clientToken = chatInited.token) }
        userInfoRepository.updateConfiguration { copy(clientToken = chatInited.token) }

        if (chatInited.status in ACTIVE_STATUSES) {
            firstMessageMutex.withLock {
                firstMessageLock?.unlockSafe()
            }
        }

        val ids = model.messages.map(UsedeskMessage::id)
        val filteredMessages = chatInited.messages.filter { it.id !in ids }
        val notSentMessages = cachedMessagesRepository.getNotSentMessages()
            .mapNotNull { it as? UsedeskMessage }
        val filteredNotSentMessages = notSentMessages.filter { it.id !in ids }
        val needToResendMessages = model.messages.isNotEmpty()
        onMessagesNew(new = filteredMessages + filteredNotSentMessages)

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
        private var listeners = setOf<IUsedeskActionListener>()

        fun add(listener: IUsedeskActionListener) {
            doLocked { listeners = listeners + listener }
        }

        fun remove(listener: IUsedeskActionListener) {
            doLocked { listeners = listeners - listener }
        }

        fun isNoListeners() = doLocked { listeners.isEmpty() }

        override fun onModel(
            model: Model,
            newMessages: List<UsedeskMessage>,
            updatedMessages: List<UsedeskMessage>,
            removedMessages: List<UsedeskMessage>
        ) {
            doLocked { listeners }.forEach {
                it.onModel(
                    model,
                    newMessages,
                    updatedMessages,
                    removedMessages
                )
            }
        }

        private fun <T> doLocked(onDo: () -> T): T =
            runBlocking { listenersMutex.withLock { onDo() } }

        override fun onException(usedeskException: Exception) {
            doLocked { listeners }.forEach { it.onException(usedeskException) }
        }
    }

    class Queue<T>(
        private val scope: CoroutineScope,
        private val onValue: suspend (T) -> Unit
    ) {
        private val emitQueue = mutableListOf<T>()
        private val flow = MutableSharedFlow<T>()
        private val mutex = Mutex()
        private var emitJob: Job? = null

        init {
            scope.launch {
                flow.collect(onValue)
            }
        }

        suspend fun collect(collector: FlowCollector<T>) {
            flow.collect(collector)
        }

        fun emit(value: T) {
            runBlocking {
                mutex.withLock {
                    emitQueue.add(value)
                    emitJob = emitJob ?: launchEmit()
                }
            }
        }

        private fun launchEmit() = scope.launch {
            while (true) {
                when (val emitValue = mutex.withLock { emitQueue.removeFirstOrNull() }) {
                    null -> mutex.withLock {
                        if (emitQueue.isEmpty()) {
                            emitJob = null
                        }
                        emitJob
                    } ?: break
                    else -> flow.emit(emitValue)
                }
            }
        }
    }

    companion object {
        private val ACTIVE_STATUSES = listOf(1, 5, 6, 8)
        private const val REPEAT_DELAY = 5000L
    }
}