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

    private var draftJob: Job? = null
    private var saveJob: Job? = null

    private val mutex = Mutex()

    private val deferredCachedUriMap = hashMapOf<Uri, Deferred<Uri>>()

    init {
        val userKey = findUserKey()

        if (userKey != null) {
            runBlocking {
                mutex.withLock {
                    val messageDraft = messagesRepository.getDraft(userKey)
                    messagesRepository.setDraft(
                        userKey,
                        messageDraft.copy(
                            files = if (configuration.cacheMessagesWithFile) {
                                messageDraft.files
                            } else {
                                listOf()
                            }
                        )
                    )
                    deferredCachedUriMap.putAll(messageDraft.files.map {
                        it.uri to CompletableDeferred(it.uri)
                    })
                }
            }
        }
    }


    override fun getNotSentMessages(): List<UsedeskMessageClient> {
        val userKey = requireUserKey()
        return messagesRepository.getNotSentMessages(userKey)
    }

    override fun addNotSentMessage(notSentMessage: UsedeskMessageClient) {
        val userKey = requireUserKey()
        messagesRepository.addNotSentMessage(userKey, notSentMessage)
    }

    override fun updateNotSentMessage(notSentMessage: UsedeskMessageClient) {
        val requireUserKey = requireUserKey()
        messagesRepository.removeNotSentMessage(requireUserKey, notSentMessage)
        messagesRepository.addNotSentMessage(requireUserKey, notSentMessage)
    }

    override fun removeNotSentMessage(notSentMessage: UsedeskMessageClient) {
        val userKey = requireUserKey()
        messagesRepository.removeNotSentMessage(userKey, notSentMessage)
    }

    override suspend fun getCachedFileAsync(uri: Uri): Deferred<Uri> {
        return mutex.withLock {
            getCachedFileInnerAsync(uri)
        }
    }

    private fun getCachedFileInnerAsync(uri: Uri): Deferred<Uri> {
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
                yield()
                val userKey = requireUserKey()
                yield()
                mutex.withLock {
                    val messageDraft = messagesRepository.getDraft(userKey)
                    messagesRepository.setDraft(userKey, messageDraft.copy(
                        files = messageDraft.files.mapNotNull {
                            val deferredCachedUri = deferredCachedUriMap[it.uri]
                            try {
                                val cachedUri = deferredCachedUri?.getCompleted()!!
                                it.copy(uri = cachedUri)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    ))
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
            val userKey = requireUserKey()
            val oldMessageDraft = messagesRepository.getDraft(userKey)
            messagesRepository.setDraft(userKey, messageDraft)
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
                    deferredCachedUriMap[uri] = getCachedFileInnerAsync(uri)
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
            val userKey = requireUserKey()
            messagesRepository.getDraft(userKey)
        }
    }

    override fun getNextLocalId(): Long {
        val userKey = requireUserKey()
        return messagesRepository.getNextLocalId(userKey)
    }

    private fun findUserKey(): String? {
        val config = userInfoRepository.getConfigurationNullable(this.configuration)
            ?: configuration
        val token = config.clientToken
        return if (token?.isNotEmpty() == true) {
            token
        } else {
            null
        }
    }

    private fun requireUserKey(): String {
        return findUserKey() ?: throw RuntimeException("Can't find configuration")
    }
}