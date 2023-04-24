
package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import ru.usedesk.common_sdk.api.entity.UsedeskApiError

internal interface LoadSections {
    class Request(
        val apiToken: String,
        val accountId: String
    )

    class Response(
        val items: Array<SectionResponse?>? = null
    ) : UsedeskApiError()
}