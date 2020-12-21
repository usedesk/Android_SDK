package ru.usedesk.knowledgebase_sdk

import android.content.Context
import ru.usedesk.knowledgebase_sdk.di.InstanceBoxUsedesk
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

object UsedeskKnowledgeBaseSdk {
    private var instanceBox: InstanceBoxUsedesk? = null
    private var configuration: UsedeskKnowledgeBaseConfiguration? = null

    @JvmStatic
    fun init(appContext: Context): IUsedeskKnowledgeBase {
        return (instanceBox ?: InstanceBoxUsedesk(appContext, requireConfiguration()).also {
            instanceBox = it
        }).knowledgeBaseSdk
    }

    @JvmStatic
    fun getInstance(): IUsedeskKnowledgeBase {
        return instanceBox?.knowledgeBaseSdk
                ?: throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.init(...) before")
    }

    @JvmStatic
    fun setConfiguration(knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration) {
        configuration = knowledgeBaseConfiguration
    }

    @JvmStatic
    fun release() {
        instanceBox?.also {
            it.release()
            instanceBox = null
        }
    }

    private fun requireConfiguration(): UsedeskKnowledgeBaseConfiguration {
        return configuration
                ?: throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.setConfiguration(...) before")
    }
}