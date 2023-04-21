
package ru.usedesk.knowledgebase_sdk.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.usedesk.common_sdk.utils.UsedeskValidatorUtil

@Parcelize
class UsedeskKnowledgeBaseConfiguration @JvmOverloads constructor(
    val urlApi: String = "https://secure.usedesk.ru",
    val accountId: String,
    val token: String,
    val clientEmail: String? = null,
    val clientName: String? = null
) : Parcelable {
    fun validate(): Validation = Validation(
        validUrlApi = UsedeskValidatorUtil.isValidUrlNecessary(urlApi),
        validKbId = accountId.isNotEmpty(),
        validToken = token.isNotEmpty(),
        validClientEmail = UsedeskValidatorUtil.isValidEmail(clientEmail)
    )

    class Validation(
        val validUrlApi: Boolean = false,
        val validKbId: Boolean = false,
        val validToken: Boolean = false,
        val validClientEmail: Boolean = false
    ) {
        fun isAllValid(): Boolean = validUrlApi
                && validKbId
                && validToken
                && validClientEmail
    }
}