
package ru.usedesk.chat_sdk.di.preparation

import dagger.Component
import ru.usedesk.chat_sdk.di.IRelease
import ru.usedesk.chat_sdk.di.common.CommonChatComponent
import ru.usedesk.chat_sdk.di.common.CommonChatDeps
import ru.usedesk.chat_sdk.domain.IUsedeskPreparation

@PreparationScope
@Component(
    modules = [PreparationModule::class],
    dependencies = [CommonChatDeps::class]
)
internal interface PreparationComponent : CommonChatDeps {

    val preparationInteractor: IUsedeskPreparation

    @Component.Factory
    interface Factory {
        fun create(commonChatDeps: CommonChatDeps): PreparationComponent
    }

    companion object {
        var preparationComponent: PreparationComponent? = null
            private set

        fun open(commonChatComponent: CommonChatComponent) = preparationComponent
            ?: DaggerPreparationComponent.factory()
                .create(commonChatComponent)
                .also { preparationComponent = it }

        fun close() {
            (preparationComponent?.preparationInteractor as? IRelease)?.release()
            preparationComponent = null
        }
    }
}