package ru.usedesk.knowledgebase_gui.screens.sections

import io.reactivex.Completable
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection
import java.util.concurrent.TimeUnit

internal class SectionsViewModel : UsedeskViewModel<SectionsViewModel.Model>(Model()) {

    init {
        reload()
    }

    private fun reload() {
        setModel { model ->//TODO: из-за этого при перезагрузке мерцает ошибка
            model.copy(
                sections = listOf(),
                loading = true
            )
        }
        doIt(UsedeskKnowledgeBaseSdk.requireInstance()
            .getSectionsRx(), {
            setModel { model ->
                model.copy(
                    sections = it,
                    loading = null
                )
            }
        }, {
            setModel { model ->
                model.copy(
                    sections = listOf(),
                    loading = false
                )
            }
            doIt(Completable.timer(3, TimeUnit.SECONDS), {
                reload()
            })
        })
    }

    data class Model(
        val sections: List<UsedeskSection> = listOf(),
        val loading: Boolean? = true
    )
}