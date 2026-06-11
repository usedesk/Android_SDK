package ru.usedesk.common_sdk.api

interface UsedeskApiFactory {
    fun <API> getInstance(baseUrl: String, apiClass: Class<API>): API
}

@Deprecated(
    message = "Use ru.usedesk.common_sdk.api.UsedeskApiFactory",
    replaceWith = ReplaceWith(
        "UsedeskApiFactory",
        "ru.usedesk.common_sdk.api.UsedeskApiFactory"
    )
)
typealias IUsedeskApiFactory = UsedeskApiFactory
