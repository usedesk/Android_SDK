package ru.usedesk.knowledgebase_gui.screens.sections

import io.reactivex.Completable
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter.State
import ru.usedesk.common_gui.UsedeskViewModel
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection
import java.util.concurrent.TimeUnit

internal class SectionsViewModel : UsedeskViewModel<SectionsViewModel.Model>(Model()) {

    init {
        reload()
    }

    private fun reload() {
        setModel {
            copy(
                sections = listOf(),
                state = when (state) {
                    State.LOADING -> State.LOADING
                    else -> State.RELOADING
                }
            )
        }
        doIt(UsedeskKnowledgeBaseSdk.requireInstance()
            .getSectionsRx(), {
            setModel {
                copy(
                    sections = it,
                    state = State.LOADED
                )
            }
        }, {
            setModel {
                copy(
                    sections = listOf(),
                    state = State.FAILED
                )
            }
            doIt(Completable.timer(3, TimeUnit.SECONDS), this::reload)
        })
    }

    data class Model(
        val sections: List<UsedeskSection> = listOf(),
        val state: State = State.LOADING
    )
}