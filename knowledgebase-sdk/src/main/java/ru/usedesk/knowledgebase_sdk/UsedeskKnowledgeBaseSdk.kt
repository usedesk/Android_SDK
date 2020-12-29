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
        if (instanceBox == null) {
            checkConfiguration()
            instanceBox = InstanceBoxUsedesk(appContext, configuration!!)
        }
        return instanceBox!!.knowledgeBaseSdk
    }

    @JvmStatic
    fun getInstance(): IUsedeskKnowledgeBase {
        if (instanceBox == null) {
            throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.init(...) before")
        }
        return instanceBox!!.knowledgeBaseSdk
    }

    @JvmStatic
    fun setConfiguration(knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration) {
        configuration = knowledgeBaseConfiguration
    }

    @JvmStatic
    fun release() {
        if (instanceBox != null) {
            instanceBox!!.release()
            instanceBox = null
        }
    }

    private fun checkConfiguration() {
        if (configuration == null) {
            throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.setConfiguration(...) before")
        }
    }
}