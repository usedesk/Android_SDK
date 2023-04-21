
package ru.usedesk.common_sdk.api

interface IUsedeskApiFactory {
    fun <API> getInstance(baseUrl: String, apiClass: Class<API>): API
}