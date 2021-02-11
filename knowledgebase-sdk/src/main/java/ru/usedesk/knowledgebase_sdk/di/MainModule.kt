package ru.usedesk.knowledgebase_sdk.di

import ru.usedesk.knowledgebase_sdk.data.repository.api.IKnowledgeBaseApiRepository
import ru.usedesk.knowledgebase_sdk.data.repository.api.KnowledgeBaseApiRepository
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.domain.KnowledgeBaseInteractor
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import toothpick.config.Module

internal class MainModule(
        knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
) : Module() {

    init {
        bind(UsedeskKnowledgeBaseConfiguration::class.java).toInstance(knowledgeBaseConfiguration)
        bind(IUsedeskKnowledgeBase::class.java).to(KnowledgeBaseInteractor::class.java)
        bind(IKnowledgeBaseApiRepository::class.java).to(KnowledgeBaseApiRepository::class.java)
    }
}