
package ru.usedesk.knowledgebase_gui._di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration

@KbUiScope
@Component(modules = [KbUiModule::class])
internal interface KbUiComponent {

    val interactor: IKnowledgeBaseInteractor

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance appContext: Context,
            @BindsInstance configuration: UsedeskKnowledgeBaseConfiguration
        ): KbUiComponent
    }

    companion object {
        var kbUiComponent: KbUiComponent? = null
            private set

        fun open(
            context: Context,
            configuration: UsedeskKnowledgeBaseConfiguration
        ) = kbUiComponent ?: DaggerKbUiComponent.factory()
            .create(
                context.applicationContext,
                configuration
            ).also { kbUiComponent = it }

        fun close() {
            kbUiComponent = null
            UsedeskKnowledgeBaseSdk.release()
        }

        fun require(): KbUiComponent =
            kbUiComponent ?: throw RuntimeException("KbUiComponent is not open")
    }
}