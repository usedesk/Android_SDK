package ru.usedesk.knowledgebase_sdk.di

import dagger.Component
import ru.usedesk.common_sdk.di.UsedeskCommonModule
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase

@Component(modules = [UsedeskCommonModule::class, KnowledgeBaseComponent::class])
interface KnowledgeBaseComponent {
    val knowledgeBaseInteractor: IUsedeskKnowledgeBase
}