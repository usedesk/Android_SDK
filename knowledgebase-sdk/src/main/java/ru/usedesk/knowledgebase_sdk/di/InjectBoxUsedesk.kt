package ru.usedesk.knowledgebase_sdk.di

import android.content.Context
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import javax.inject.Inject

internal class InjectBoxUsedesk(
    appContext: Context,
    knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
) {
    private val knowledgeBaseComponent = DaggerKnowledgeBaseComponent.builder()
        .bindAppContext(appContext)
        .bindKnowledgeBaseConfiguration(knowledgeBaseConfiguration)
        .build()

    val knowledgeBaseInteractor = knowledgeBaseComponent.knowledgeBaseInteractor

    fun release() {


    }
}