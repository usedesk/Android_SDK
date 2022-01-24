package ru.usedesk.knowledgebase_sdk.di

import android.content.Context
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

internal class InjectBoxUsedesk(
    appContext: Context,
    knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
) {
    private var knowledgeBaseComponent: KnowledgeBaseComponent?

    val knowledgeBaseInteractor: IUsedeskKnowledgeBase

    init {
        val knowledgeBaseComponent = DaggerKnowledgeBaseComponent.builder()
            .bindAppContext(appContext)
            .bindKnowledgeBaseConfiguration(knowledgeBaseConfiguration)
            .build()

        this.knowledgeBaseComponent = knowledgeBaseComponent
        this.knowledgeBaseInteractor = knowledgeBaseComponent.knowledgeBaseInteractor
    }

    fun release() {
        knowledgeBaseComponent = null
    }
}