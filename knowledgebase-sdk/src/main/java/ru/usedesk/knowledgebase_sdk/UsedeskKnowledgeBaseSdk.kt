
package ru.usedesk.knowledgebase_sdk

import android.content.Context
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.di.KbComponent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

object UsedeskKnowledgeBaseSdk {
    private var configuration: UsedeskKnowledgeBaseConfiguration? = null
    private val mutex = Mutex()

    @JvmStatic
    fun setConfiguration(knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration) {
        configuration = knowledgeBaseConfiguration
    }

    @JvmStatic
    fun requireConfiguration(): UsedeskKnowledgeBaseConfiguration = configuration
        ?: throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.setConfiguration(...) before")

    @JvmStatic
    fun init(
        context: Context,
        configuration: UsedeskKnowledgeBaseConfiguration = requireConfiguration()
    ): IUsedeskKnowledgeBase = runBlocking {
        mutex.withLock {
            setConfiguration(configuration)

            KbComponent.open(
                context,
                configuration
            ).usedeskKb
        }
    }

    @JvmStatic
    fun getInstance(): IUsedeskKnowledgeBase? = KbComponent.kbComponent?.usedeskKb

    @JvmStatic
    fun requireInstance(): IUsedeskKnowledgeBase = getInstance()
        ?: throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.init(...) before")

    @JvmStatic
    fun release() {
        runBlocking {
            mutex.withLock {
                KbComponent.close()
            }
        }
    }
}