package ru.usedesk.knowledgebase_sdk

import android.content.Context
import ru.usedesk.knowledgebase_sdk.di.InjectBoxUsedesk
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

object UsedeskKnowledgeBaseSdk {
    private var injectBox: InjectBoxUsedesk? = null
    private var configuration: UsedeskKnowledgeBaseConfiguration? = null

    @JvmStatic
    fun setConfiguration(knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration) {
        configuration = knowledgeBaseConfiguration
    }

    @JvmStatic
    fun requireConfiguration(): UsedeskKnowledgeBaseConfiguration {
        return configuration
                ?: throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.setConfiguration(...) before")
    }

    @JvmStatic
    fun init(context: Context): IUsedeskKnowledgeBase {
        return (injectBox ?: InjectBoxUsedesk(context, requireConfiguration()).also {
            injectBox = it
        }).knowledgeBaseInteractor
    }

    @JvmStatic
    fun getInstance(): IUsedeskKnowledgeBase? {
        return injectBox?.knowledgeBaseInteractor
    }

    @JvmStatic
    fun requireInstance(): IUsedeskKnowledgeBase {
        return getInstance()
                ?: throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.init(...) before")
    }

    @JvmStatic
    fun release() {
        injectBox?.also {
            it.release()
            injectBox = null
        }
    }
}