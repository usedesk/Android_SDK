
package ru.usedesk.chat_sdk.di.preparation

import dagger.Binds
import dagger.Module
import ru.usedesk.chat_sdk.domain.IUsedeskPreparation
import ru.usedesk.chat_sdk.domain.PreparationInteractor
import javax.inject.Scope

@Module(includes = [PreparationModuleProvides::class, PreparationModuleBinds::class])
internal interface PreparationModule

@Module
internal class PreparationModuleProvides

@Module
internal interface PreparationModuleBinds {
    @[Binds PreparationScope]
    fun prepareInteractor(interactor: PreparationInteractor): IUsedeskPreparation
}

@Scope
annotation class PreparationScope
