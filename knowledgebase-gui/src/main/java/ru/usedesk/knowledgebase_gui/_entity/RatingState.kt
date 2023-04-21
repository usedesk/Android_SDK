
package ru.usedesk.knowledgebase_gui._entity

internal sealed interface RatingState {
    class Required(val error: Boolean? = null) : RatingState
    class Sending(val good: Boolean) : RatingState
    class Sent(val good: Boolean) : RatingState
}