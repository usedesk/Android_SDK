
package ru.usedesk.chat_sdk.entity

class UsedeskOfflineFormSettings(
    val noOperators: Boolean,
    val workType: WorkType,
    val callbackTitle: String = "",
    val callbackGreeting: String = "",
    val fields: List<CustomField> = listOf(),
    val topics: List<String> = listOf(),
    val topicsTitle: String = "",
    val topicsRequired: Boolean = false
) {
    class CustomField(
        val key: String,
        val required: Boolean,
        val checked: Boolean,
        val placeholder: String
    )

    enum class WorkType {
        NEVER,
        CHECK_WORKING_TIMES,
        ALWAYS_ENABLED_CALLBACK_WITHOUT_CHAT,
        ALWAYS_ENABLED_CALLBACK_WITH_CHAT
    }
}