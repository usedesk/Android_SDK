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
    fun requireConfiguration(): UsedeskKnowledgeBaseConfiguration = configuration
        ?: throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.setConfiguration(...) before")

    @JvmStatic
    fun init(context: Context): IUsedeskKnowledgeBase =
        (injectBox ?: InjectBoxUsedesk(context, requireConfiguration()).also {
            injectBox = it
        }).knowledgeBaseInteractor

    @JvmStatic
    fun getInstance(): IUsedeskKnowledgeBase? = injectBox?.knowledgeBaseInteractor

    @JvmStatic
    fun requireInstance(): IUsedeskKnowledgeBase = getInstance()
        ?: throw RuntimeException("Must call UsedeskKnowledgeBaseSdk.init(...) before")

    @JvmStatic
    fun release() {
        injectBox?.also {
            it.release()
            injectBox = null
        }
    }
}