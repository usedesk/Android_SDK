package ru.usedesk.knowledgebase_gui._entity

internal sealed interface ReviewState {
    object Required : ReviewState
    object Sending : ReviewState
    object Sent : ReviewState
    class Failed(val code: Int? = null) : ReviewState
}