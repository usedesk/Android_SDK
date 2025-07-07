package ru.usedesk.chat_sdk.data.repository.messages

import android.net.Uri
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import ru.usedesk.chat_sdk.data.repository.api.loader.file.IFileLoader
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskFile
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.chat_sdk.entity.UsedeskMessage
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientAudio
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientFile
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientImage
import ru.usedesk.chat_sdk.entity.UsedeskMessageClientVideo
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import ru.usedesk.chat_sdk.entity.UsedeskMessageOwner
import java.util.Calendar
import javax.inject.Inject

//TODO: do something with repository hierarchy
internal class CachedMessagesRepository @Inject constructor(
    initConfiguration: UsedeskChatConfiguration,
    private val messagesRepository: IUsedeskMessagesRepository,
    private val fileLoader: IFileLoader
) : ICachedMessagesRepository {

    private var draftJob: Job? = null
    private var saveJob: Job? = null

    private val mutex = Mutex()

    private val deferredCachedUriMap = hashMapOf<Uri, Deferred<Uri?>>()
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        runBlocking {
            mutex.withLock {
                val messageDraft = messagesRepository.getDraft()
                messagesRepository.setDraft(
                    messageDraft.copy(
                        files = when {
                            initConfiguration.cacheMessagesWithFile -> messageDraft.files
                            else -> listOf()
                        }
                    )
                )
                val cachedFiles = messageDraft.files
                    .map { it.uri to CompletableDeferred(it.uri) }
                deferredCachedUriMap.putAll(cachedFiles)
            }
        }
    }

    override suspend fun createSendingMessage(
        fileInfo: UsedeskFileInfo,
        localId: String
    ): UsedeskMessage.File {
        val calendar = Calendar.getInstance()
        val cachedUri = getCachedFileAsync(fileInfo.uri).await()
        val file = UsedeskFile.create(
            cachedUri.toString(),
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
        }.also {
            addNotSentMessage(it)
        }
    }

    override suspend fun getNotSentMessages(): List<UsedeskMessageOwner.Client> {
        return messagesRepository.getNotSentMessages()
    }

    override suspend fun addNotSentMessage(notSentMessage: UsedeskMessageOwner.Client) {
        messagesRepository.addNotSentMessage(notSentMessage)
    }

    override suspend fun updateNotSentMessage(notSentMessage: UsedeskMessageOwner.Client) {
        messagesRepository.removeNotSentMessage(notSentMessage.localId)
        messagesRepository.addNotSentMessage(notSentMessage)
    }

    override suspend fun removeNotSentMessage(localId: String) {
        messagesRepository.removeNotSentMessage(localId)
    }

    override suspend fun getCachedFileAsync(uri: Uri): Deferred<Uri?> = mutex.withLock {
        getCachedFileInnerAsync(uri)
    }

    private fun getCachedFileInnerAsync(uri: Uri) = deferredCachedUriMap.getOrPut(uri) {
        ioScope.async {
            fileLoader.save(uri)
        }
    }

    override suspend fun removeFileFromCache(uri: Uri) {
        mutex.withLock {
            removeDeferredCache(uri)
        }
    }

    private suspend fun removeDeferredCache(uri: Uri) {
        deferredCachedUriMap.remove(uri)?.run {
            cancel()
            if (isCompleted) {
                val cachedUri = await()
                if (cachedUri != null) {
                    fileLoader.remove(cachedUri)
                }
            }
        }
    }

    private fun updateMessageDraft(now: Boolean) {
        if (now) {
            draftJob?.cancel()
            draftJob = null
            saveJob?.cancel()
            saveJob = ioScope.launch {
                mutex.withLock {
                    val messageDraft = messagesRepository.getDraft()
                    messagesRepository.setDraft(
                        messageDraft.copy(
                            files = messageDraft.files.mapNotNull { fileInfo ->
                                val deferredCachedUri = deferredCachedUriMap[fileInfo.uri]
                                deferredCachedUri?.await()?.let { cachedUri ->
                                    fileInfo.copy(uri = cachedUri)
                                }
                            }
                        )
                    )
                }
            }
        } else {
            draftJob = draftJob ?: ioScope.launch {
                delay(2000)
                yield()
                mutex.withLock {
                    updateMessageDraft(true)
                }
            }
        }
    }

    override suspend fun setMessageDraft(
        messageDraft: UsedeskMessageDraft,
        cacheFiles: Boolean
    ): UsedeskMessageDraft = mutex.withLock {
        messagesRepository.getDraft().also { oldMessageDraft ->
            messagesRepository.setDraft(messageDraft)
            if (cacheFiles) {
                val oldFiles = oldMessageDraft.files.map(UsedeskFileInfo::uri)
                val newFiles = messageDraft.files.map(UsedeskFileInfo::uri)
                oldFiles.filter { it !in newFiles }
                    .forEach { removeDeferredCache(it) }
                newFiles.filter { it !in oldFiles }
                    .map { uri -> deferredCachedUriMap[uri] = getCachedFileInnerAsync(uri) }
            }
            updateMessageDraft(messageDraft.text.isEmpty() && messageDraft.files.isEmpty())
        }
    }

    override suspend fun getMessageDraft(): UsedeskMessageDraft = mutex.withLock {
        messagesRepository.getDraft()
    }

    override suspend fun getNextLocalId() = messagesRepository.getNextLocalId()
}