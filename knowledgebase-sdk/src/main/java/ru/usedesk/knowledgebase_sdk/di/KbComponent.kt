
package ru.usedesk.knowledgebase_sdk.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.usedesk.common_sdk.di.UsedeskCommonModule
import ru.usedesk.knowledgebase_sdk.data.repository.api.IUsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

@KbScope
@Component(modules = [UsedeskCommonModule::class, KbModule::class])
internal interface KbComponent {

    val usedeskKb: IUsedeskKnowledgeBase

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance appContext: Context,
            @BindsInstance configuration: UsedeskKnowledgeBaseConfiguration
        ): KbComponent
    }

    companion object {
        var kbComponent: KbComponent? = null
            private set

        fun open(
            context: Context,
            configuration: UsedeskKnowledgeBaseConfiguration
        ) = kbComponent ?: DaggerKbComponent.factory()
            .create(
                context.applicationContext,
                configuration
            ).also { kbComponent = it }

        fun close() {
            kbComponent = null
        }
    }
}