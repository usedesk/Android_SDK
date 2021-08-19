package ru.usedesk.chat_sdk.domain

import android.net.Uri
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import ru.usedesk.chat_sdk.data.repository.messages.IUsedeskMessagesRepository
import ru.usedesk.chat_sdk.di.MainModule
import ru.usedesk.chat_sdk.entity.UsedeskMessageClient
import ru.usedesk.chat_sdk.entity.UsedeskMessageDraft
import toothpick.InjectConstructor
import java.util.concurrent.TimeUnit

@InjectConstructor
internal class CachedMessagesInteractor(
    private val messagesRepository: IUsedeskMessagesRepository,
    private val constants: MainModule.Constants
) : ICachedMessagesInteractor {

    private var messageDraft: UsedeskMessageDraft =
        messagesRepository.getDraft() ?: UsedeskMessageDraft()

    private var draftDisposable: Disposable? = null

    private val cachedFileUriMap = messageDraft.files.map {
        it.uri to it.uri
    }.toMap().toMutableMap()

    @Synchronized
    override fun getNotSentMessages(): List<UsedeskMessageClient> {
        return messagesRepository.getNotSentMessages()
    }

    @Synchronized
    override fun addNotSentMessage(notSentMessage: UsedeskMessageClient) {
        messagesRepository.addNotSentMessage(notSentMessage)
    }

    @Synchronized
    override fun removeNotSentMessage(notSentMessage: UsedeskMessageClient) {
        messagesRepository.removeNotSentMessage(notSentMessage)
    }

    @Synchronized
    override fun getCachedUri(uri: Uri): Uri {
        return cachedFileUriMap[uri] ?: addFileToCache(uri)
    }

    @Synchronized
    override fun addFileToCache(uri: Uri): Uri {
        return if (constants.cacheMessagesWithFile) {
            messagesRepository.addFileToCache(uri)
        } else {
            uri
        }
    }

    @Synchronized
    override fun removeFileFromCache(uri: Uri) {
        if (constants.cacheMessagesWithFile) {
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
        messagesRepository.setDraft(messageDraft)
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
            }.forEach {
                cachedFileUriMap[it] = addFileToCache(it)
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
        return messagesRepository.getNextLocalId()
    }
}