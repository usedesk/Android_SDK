package ru.usedesk.chat_sdk.domain

import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskMessageClient
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft

internal class CachedMessagesInteractor(
    private val configuration: UsedeskChatConfiguration,
    private val messagesRepository: IUsedeskMessagesRepository,
    private val userInfoRepository: IUserInfoRepository
) : ICachedMessagesInteractor {

    private var messageDraft: UsedeskMessageDraft

    private var draftJob: Job? = null
    private var saveJob: Job? = null

    private val mutex = Mutex()

    private val deferredCachedUriMap = hashMapOf<Uri, Deferred<Uri>>()

    init {
        val token = getUserKey()
        messageDraft = if (token != null) {
            val draft = messagesRepository.getDraft(token)
            draft.copy(
                files = if (configuration.cacheMessagesWithFile) {
                    draft.files
                } else {
                    listOf()
                }
            )
        } else {
            null
        } ?: UsedeskMessageDraft()

        deferredCachedUriMap.putAll(messageDraft.files.map {
            it.uri to CompletableDeferred(it.uri)
        })
    }


    override fun getNotSentMessages(): List<UsedeskMessageClient> {
        val token = getUserKey()
        return if (token != null) {
            messagesRepository.getNotSentMessages(token)
        } else {
            listOf()
        }
    }

    override fun addNotSentMessage(notSentMessage: UsedeskMessageClient) {
        val token = getUserKey()
        if (token != null) {
            messagesRepository.addNotSentMessage(token, notSentMessage)
        }
    }

    override fun updateNotSentMessage(notSentMessage: UsedeskMessageClient) {
        val token = getUserKey()
        if (token != null) {
            messagesRepository.removeNotSentMessage(token, notSentMessage)
            messagesRepository.addNotSentMessage(token, notSentMessage)
        }
    }

    override fun removeNotSentMessage(notSentMessage: UsedeskMessageClient) {
        val token = getUserKey()
        if (token != null) {
            messagesRepository.removeNotSentMessage(token, notSentMessage)
        }
    }

    override suspend fun getCachedFileAsync(uri: Uri): Deferred<Uri> {
        return mutex.withLock {
            getCachedFile(uri)
        }
    }

    private fun getCachedFile(uri: Uri): Deferred<Uri> {
        return deferredCachedUriMap[uri] ?: CoroutineScope(Dispatchers.IO).async {
            messagesRepository.addFileToCache(uri)
        }.also {
            deferredCachedUriMap[uri] = it
        }
    }

    override suspend fun removeFileFromCache(uri: Uri) {
        if (configuration.cacheMessagesWithFile) {
            mutex.withLock {
                removeDeferredCache(uri)
            }
        }
    }

    private suspend fun removeDeferredCache(uri: Uri) {
        val removedDeferred = deferredCachedUriMap.remove(uri)?.apply {
            cancel()
        }
        if (removedDeferred?.isCompleted == true) {
            val cachedUri = removedDeferred.await()
            messagesRepository.removeFileFromCache(cachedUri)
        }
    }

    private suspend fun updateMessageDraft(now: Boolean) {
        if (now) {
            draftJob?.cancel()
            draftJob = null
            saveJob?.cancel()
            saveJob = CoroutineScope(Dispatchers.IO).launch {
                val configuration =
                    userInfoRepository.getConfiguration(this@CachedMessagesInteractor.configuration)
                yield()
                val token = configuration.clientToken
                if (token != null) {
                    yield()
                    mutex.withLock {
                        val messageDraft = this@CachedMessagesInteractor.messageDraft.copy(
                            files = this@CachedMessagesInteractor.messageDraft.files.mapNotNull {
                                val deferredCachedUri = deferredCachedUriMap[it.uri]
                                try {
                                    val cachedUri = deferredCachedUri?.getCompleted()!!
                                    it.copy(uri = cachedUri)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        )
                        messagesRepository.setDraft(token, messageDraft)
                    }
                }
            }
        } else {
            if (draftJob == null) {
                draftJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(2000)
                    yield()
                    mutex.withLock {
                        updateMessageDraft(true)
                    }
                }
            }
        }
    }


    override suspend fun setMessageDraft(
        messageDraft: UsedeskMessageDraft,
        cacheFiles: Boolean
    ): UsedeskMessageDraft {
        return mutex.withLock {
            val oldMessageDraft = this.messageDraft
            this.messageDraft = messageDraft
            if (cacheFiles) {
                val oldFiles = oldMessageDraft.files.map {
                    it.uri
                }
                val newFiles = messageDraft.files.map {
                    it.uri
                }
                oldFiles.filter {
                    it !in newFiles
                }.forEach {
                    removeDeferredCache(it)
                }
                newFiles.filter {
                    it !in oldFiles
                }.map { uri ->
                    deferredCachedUriMap[uri] = getCachedFile(uri)
                }
            }
            updateMessageDraft(
                messageDraft.text.isEmpty() &&
                        messageDraft.files.isEmpty()
            )
            oldMessageDraft
        }
    }

    override suspend fun getMessageDraft(): UsedeskMessageDraft {
        return mutex.withLock {
            messageDraft
        }
    }

    override fun getNextLocalId(): Long {
        val token = getUserKey()
        return if (token != null) {
            messagesRepository.getNextLocalId(token)
        } else {
            -1L
        }
    }

    private fun getUserKey(): String? {
        val config = userInfoRepository.getConfigurationNullable(this.configuration)
            ?: configuration
        val token = config.clientToken
        return if (token?.isNotEmpty() == true) {
            token
        } else {
            null
        }
    }
}