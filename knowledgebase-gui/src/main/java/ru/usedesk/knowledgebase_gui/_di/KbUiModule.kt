
package ru.usedesk.knowledgebase_gui._di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.usedesk.knowledgebase_gui.domain.IKnowledgeBaseInteractor
import ru.usedesk.knowledgebase_gui.domain.KnowledgeBaseInteractor
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import javax.inject.Scope

@Module(includes = [KbUiModuleProvides::class, KbUiModuleBinds::class])
internal interface KbUiModule

@Module
internal class KbUiModuleProvides {
    @[Provides KbUiScope]
    fun usedeskKb(
        appContext: Context,
        configuration: UsedeskKnowledgeBaseConfiguration
    ) = UsedeskKnowledgeBaseSdk.init(appContext, configuration)
}

@Module
internal interface KbUiModuleBinds {
    @[Binds KbUiScope]
    fun interactor(interactor: KnowledgeBaseInteractor): IKnowledgeBaseInteractor
}

@Scope
internal annotation class KbUiScope