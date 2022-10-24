package ru.usedesk.knowledgebase_sdk.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.usedesk.common_sdk.di.UsedeskCommonModule
import ru.usedesk.knowledgebase_sdk.domain.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

@KnowledgeBaseScope
@Component(modules = [UsedeskCommonModule::class, KnowledgeBaseModule::class])
internal interface KnowledgeBaseComponent {

    val knowledgeBaseInteractor: IUsedeskKnowledgeBase

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun appContext(context: Context): Builder

        @BindsInstance
        fun configuration(configuration: UsedeskKnowledgeBaseConfiguration): Builder

        fun build(): KnowledgeBaseComponent
    }
}