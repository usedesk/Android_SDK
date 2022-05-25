package ru.usedesk.knowledgebase_sdk.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.knowledgebase_sdk.data.repository.api.IKnowledgeBaseApiRepository
import ru.usedesk.knowledgebase_sdk.data.repository.api.KnowledgeBaseApiRepository
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.domain.KnowledgeBaseInteractor
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import javax.inject.Scope

@Module
internal object KnowledgeBaseModule {

    @[Provides KnowledgeBaseScope]
    fun provideApiRepository(
        knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration,
        apiFactory: IUsedeskApiFactory,
        gson: Gson
    ): IKnowledgeBaseApiRepository = KnowledgeBaseApiRepository(
        knowledgeBaseConfiguration,
        apiFactory,
        gson
    )

    @[Provides KnowledgeBaseScope]
    fun provideKnowledgeBaseInteractor(
        knowledgeBaseApiRepository: IKnowledgeBaseApiRepository
    ): IUsedeskKnowledgeBase = KnowledgeBaseInteractor(knowledgeBaseApiRepository)
}

@Scope
annotation class KnowledgeBaseScope