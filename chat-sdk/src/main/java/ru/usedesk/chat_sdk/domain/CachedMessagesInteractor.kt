package ru.usedesk.chat_sdk.domain

import android.net.Uri
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.data.repository.configuration.IUserInfoRepository
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.entity.UsedeskMessageClient
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
internal class CachedMessagesInteractor(
    private val messagesRepository: IUsedeskMessagesRepository,
    private val userInfoRepository: IUserInfoRepository,
    private val configuration: UsedeskChatConfiguration
) : ICachedMessagesInteractor {

    private var messageDraft: UsedeskMessageDraft

    private var draftDisposable: Disposable? = null

    private val cachedFileUriMap = LinkedHashMap<Uri, Uri>()

    init {
        val token = getUserKey()
        messageDraft = if (token != null) {
            messagesRepository.getDraft(token)
        } else {
            null
        } ?: UsedeskMessageDraft()

        cachedFileUriMap.putAll(messageDraft.files.map {
            it.uri to it.uri
        })
    }

    @Synchronized
    override fun getNotSentMessages(): List<UsedeskMessageClient> {
        val token = getUserKey()
        return if (token != null) {
            messagesRepository.getNotSentMessages(token)
        } else {
            listOf()
        }
    }

    @Synchronized
    override fun addNotSentMessage(notSentMessage: UsedeskMessageClient) {
        val token = getUserKey()
        if (token != null) {
            messagesRepository.addNotSentMessage(token, notSentMessage)
        }
    }

    @Synchronized
    override fun removeNotSentMessage(notSentMessage: UsedeskMessageClient) {
        val token = getUserKey()
        if (token != null) {
            messagesRepository.removeNotSentMessage(token, notSentMessage)
        }
    }

    @Synchronized
    override fun getCachedUri(uri: Uri): Uri {
        return cachedFileUriMap[uri] ?: addFileToCache(uri)
    }

    @Synchronized
    override fun addFileToCache(uri: Uri): Uri {
        return if (configuration.cacheMessagesWithFile) {
            messagesRepository.addFileToCache(uri)
        } else {
            uri
        }
    }

    @Synchronized
    override fun removeFileFromCache(uri: Uri) {
        if (configuration.cacheMessagesWithFile) {
            messagesRepository.removeFileFromCache(uri)
        }
    }

    @Synchronized
    override fun updateMessageDraft(now: Boolean) {
        if (now) {
            draftDisposable?.dispose()
            draftDisposable = null
            saveMessageDraft()
        } else if (draftDisposable == null) {
            draftDisposable = Completable.timer(2, TimeUnit.SECONDS).subscribe {
                updateMessageDraft(true)
            }
        }
    }

    private fun saveMessageDraft() {
        val messageDraft = UsedeskMessageDraft(this.messageDraft.text,
            this.messageDraft.files.map {
                val cachedUri = cachedFileUriMap[it.uri]
                it.copy(uri = cachedUri ?: it.uri)
            })
        val configuration = userInfoRepository.getConfiguration(this.configuration)
        val token = configuration.clientToken
        if (token != null) {
            messagesRepository.setDraft(token, messageDraft)
        }
    }

    @Synchronized
    override fun setMessageDraft(messageDraft: UsedeskMessageDraft, cacheFiles: Boolean) {
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
                val cachedUri = cachedFileUriMap[it]
                cachedFileUriMap.remove(it)
                if (cachedUri != null) {
                    removeFileFromCache(cachedUri)
                }
            }
            newFiles.filter {
                it !in oldFiles
            }.forEach { uri ->
                cachedFileUriMap[uri] = addFileToCache(uri)
            }
        }
        this.messageDraft = messageDraft
        updateMessageDraft(false)
    }

    @Synchronized
    override fun getMessageDraft(): UsedeskMessageDraft {
        return messageDraft
    }

    @Synchronized
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