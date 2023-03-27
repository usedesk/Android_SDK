package ru.usedesk.knowledgebase_gui._entity

internal sealed interface RatingState {
    object Required : RatingState
    class Sending(val good: Boolean) : RatingState
    class Sent(val good: Boolean) : RatingState
}