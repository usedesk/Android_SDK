package ru.usedesk.chat_sdk.entity

class UsedeskOfflineFormSettings(
        val noOperators: Boolean,
        var workType: WorkType,
        var callbackTitle: String = "",
        var callbackGreeting: String = "",
        var customFields: List<CustomField> = listOf(),
        var topics: List<String> = listOf(),
        var topicsTitle: String = "",
        var topicsRequired: Boolean = false
) {
    class CustomField(
            var required: Boolean,
            var checked: Boolean,
            var placeholder: String
    )

    enum class WorkType {
        NEVER,
        CHECK_WORKING_TIMES,
        ALWAYS_ENABLED_CALLBACK_WITHOUT_CHAT,
        ALWAYS_ENABLED_CALLBACK_WITH_CHAT
    }
}