package ru.usedesk.chat_sdk.entity

class UsedeskOfflineFormSettings(
        val noOperators: Boolean,
        var workType: WorkType,
        var callbackTitle: String? = null,
        var callbackGreeting: String? = null,
        var customFields: List<CustomField> = listOf(),
        var topics: List<String> = listOf(),
        var topicsTitle: String? = null,
        var topicsRequired: Boolean = false
) {
    class CustomField(
            var type: Type,
            var required: Boolean,
            var placeholder: String? = null
    ) {
        enum class Type {
            TEXT
        }
    }

    enum class WorkType {
        NEVER,
        CHECK_WORKING_TIMES,
        ALWAYS_ENABLED_CALLBACK_WITHOUT_CHAT,
        ALWAYS_ENABLED_CALLBACK_WITH_CHAT
    }
}