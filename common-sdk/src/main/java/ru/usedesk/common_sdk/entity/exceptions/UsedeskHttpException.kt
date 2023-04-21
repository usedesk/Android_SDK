
package ru.usedesk.common_sdk.entity.exceptions

class UsedeskHttpException(
    val error: Error = Error.UNKNOWN_ERROR,
    message: String? = null
) : UsedeskException(message) {

    override fun toString() = super.toString() + "\n" + error.toString()

    enum class Error {
        IO_ERROR,
        UNKNOWN_ERROR
    }
}