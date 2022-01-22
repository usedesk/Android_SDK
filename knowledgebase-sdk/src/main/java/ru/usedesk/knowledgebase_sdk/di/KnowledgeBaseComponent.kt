package ru.usedesk.knowledgebase_sdk.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.usedesk.common_sdk.di.UsedeskCommonModule
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.domain.KnowledgeBaseInteractor
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

@Component(modules = [UsedeskCommonModule::class, KnowledgeBaseModule::class])
internal interface KnowledgeBaseComponent {

    val knowledgeBaseInteractor: IUsedeskKnowledgeBase

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun bindAppContext(context: Context): Builder

        @BindsInstance
        fun bindKnowledgeBaseConfiguration(
            knowledgeBaseConfiguration: UsedeskKnowledgeBaseConfiguration
        ): Builder


        fun build(): KnowledgeBaseComponent
    }
}