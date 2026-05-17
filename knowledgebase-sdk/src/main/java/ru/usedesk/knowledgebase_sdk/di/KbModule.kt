
package ru.usedesk.knowledgebase_sdk.di

import dagger.Binds
import dagger.Module
import ru.usedesk.knowledgebase_sdk.data.repository.api.UsedeskKnowledgeBase
import ru.usedesk.knowledgebase_sdk.data.repository.api.KbRepository
import javax.inject.Scope

@Module(includes = [KbModuleProvides::class, KbModuleBinds::class])
internal interface KbModule

@Module
internal class KbModuleProvides

@Module
internal interface KbModuleBinds {
    @[Binds KbScope]
    fun api(api: KbRepository): UsedeskKnowledgeBase
}

@Scope
internal annotation class KbScope