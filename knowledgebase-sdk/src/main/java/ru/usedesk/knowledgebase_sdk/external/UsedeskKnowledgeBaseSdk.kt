package ru.usedesk.knowledgebase_sdk.external

import android.content.Context
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskKnowledgeBaseConfiguration
import ru.usedesk.knowledgebase_sdk.internal.di.InstanceBox

object UsedeskKnowledgeBaseSdk {
    private var instanceBox: InstanceBox? = null
    private var configuration: UsedeskKnowledgeBaseConfiguration? = null

    @JvmStatic
    fun init(appContext: Context): IUsedeskKnowledgeBase {
        if (instanceBox == null) {
            checkConfiguration()
            instanceBox = InstanceBox(appContext, configuration!!)
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