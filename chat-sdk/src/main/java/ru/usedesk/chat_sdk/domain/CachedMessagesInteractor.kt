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

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    private val deferredCachedUriMap = hashMapOf<Uri, Deferred<Uri>>()

    init {
        val token = getUserKey()
        messageDraft = if (token != null) {
            messagesRepository.getDraft(token)
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
            deferredCachedUriMap[uri] ?: ioScope.async {
                messagesRepository.addFileToCache(uri)
            }.also {
                deferredCachedUriMap[uri] = it
            }
        }
    }

    override fun removeFileFromCache(uri: Uri) {
        if (configuration.cacheMessagesWithFile) {
            messagesRepository.removeFileFromCache(uri)
        }
    }

    private suspend fun updateMessageDraft(now: Boolean) {
        if (now) {
            mutex.withLock {
                draftJob?.cancel()
                draftJob = null
                saveJob?.cancel()
                saveJob = ioScope.launch {
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
            }
        } else {
            mutex.withLock {
                if (draftJob == null) {
                    draftJob = ioScope.launch {
                        delay(2000)
                        yield()
                        updateMessageDraft(true)
                    }
                }
            }
        }
    }

    override suspend fun setMessageDraft(messageDraft: UsedeskMessageDraft, cacheFiles: Boolean) {
        if (cacheFiles) {
            val oldFiles = this.messageDraft.files.map {
                it.uri
            }
            val newFiles = messageDraft.files.map {
                it.uri
            }
            oldFiles.filter {
                it !in newFiles
            }.forEach {
                mutex.withLock {
                    val deferredCachedUri = deferredCachedUriMap[it]
                    if (deferredCachedUri?.isCompleted == true) {
                        val cachedUri = deferredCachedUri.await()
                        removeFileFromCache(cachedUri)
                    } else {
                        deferredCachedUri?.cancel()
                    }
                    deferredCachedUriMap.remove(it)
                }
            }
            newFiles.filter {
                it !in oldFiles
            }.map { uri ->
                deferredCachedUriMap[uri] = getCachedFileAsync(uri)
            }
        }
        this.messageDraft = messageDraft
        updateMessageDraft(
            messageDraft.text.isEmpty() &&
                    messageDraft.files.isEmpty()
        )
    }

    override fun getMessageDraft(): UsedeskMessageDraft = messageDraft

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