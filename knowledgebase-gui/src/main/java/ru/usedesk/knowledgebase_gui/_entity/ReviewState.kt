
package ru.usedesk.knowledgebase_gui._entity

internal sealed interface ReviewState {
    class Required(val error: Boolean = false) : ReviewState
    object Sending : ReviewState
    object Sent : ReviewState
}