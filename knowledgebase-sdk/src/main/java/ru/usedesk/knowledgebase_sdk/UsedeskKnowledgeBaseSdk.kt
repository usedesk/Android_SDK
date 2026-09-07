
package ru.usedesk.knowledgebase_sdk

import android.content.Context
import ru.usedesk.knowledgebase_sdk.data.repository.api.UsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.di.KbComponent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

object UsedeskKnowledgeBaseSdk {
    private var configuration: UsedeskKnowledgeBaseConfiguration? = null
    private val lock = Any()

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
    ): UsedeskKnowledgeBase = synchronized(lock) {
        setConfiguration(configuration)

        KbComponent.open(
            context,
            configuration
        ).usedeskKb
    }

    @JvmStatic
    fun getInstance(): UsedeskKnowledgeBase? = KbComponent.kbComponent?.usedeskKb

    @JvmStatic
    fun requireInstance(): UsedeskKnowledgeBase = getInstance()
        ?: throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.init(...) before")

    @JvmStatic
    fun release() {
        synchronized(lock) { KbComponent.close() }
    }
}