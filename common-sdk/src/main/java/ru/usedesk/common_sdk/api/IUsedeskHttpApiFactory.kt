package ru.usedesk.common_sdk.api

interface IUsedeskHttpApiFactory {
    fun <API> getInstance(baseUrl: String, apiClass: Class<API>): API
}