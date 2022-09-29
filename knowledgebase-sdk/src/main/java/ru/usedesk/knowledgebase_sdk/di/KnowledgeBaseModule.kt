package ru.usedesk.knowledgebase_sdk.di

import dagger.Binds
import dagger.Module
import ru.usedesk.knowledgebase_sdk.data.repository.api.IKnowledgeBaseApi
import ru.usedesk.knowledgebase_sdk.data.repository.api.KnowledgeBaseApi
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.domain.KnowledgeBaseInteractor
import javax.inject.Scope

@Module(includes = [KnowledgeBaseModuleProvides::class, KnowledgeBaseModuleBinds::class])
internal interface KnowledgeBaseModule

@Module
internal class KnowledgeBaseModuleProvides

@Module
internal interface KnowledgeBaseModuleBinds {
    @[Binds KnowledgeBaseScope]
    fun api(api: KnowledgeBaseApi): IKnowledgeBaseApi

    @[Binds KnowledgeBaseScope]
    fun interactor(interactor: KnowledgeBaseInteractor): IUsedeskKnowledgeBase
}

@Scope
annotation class KnowledgeBaseScope