package ru.usedesk.common_sdk.external.entity.exceptions

class UsedeskSocketException : UsedeskException {
    val error: Error

    constructor(message: String?) : this(Error.UNKNOWN_ERROR, message)

    @JvmOverloads
    constructor(error: Error = Error.UNKNOWN_ERROR) {
        this.error = error
    }

    constructor(error: Error, message: String?) : super(message) {
        this.error = error
    }

    override fun toString() = super.toString() + "\n" + error.toString()

    enum class Error {
        INTERNAL_SERVER_ERROR,
        BAD_REQUEST_ERROR,
        FORBIDDEN_ERROR,
        DISCONNECTED,
        IO_ERROR,
        JSON_ERROR,
        UNKNOWN_FROM_SERVER_ERROR,
        UNKNOWN_ERROR
    }
}