package ru.usedesk.common_sdk.entity.exceptions

class UsedeskHttpException : UsedeskException {
    val error: Error

    constructor() {
        error = Error.UNKNOWN_ERROR
    }

    constructor(message: String?) : super(message) {
        error = Error.UNKNOWN_ERROR
    }

    constructor(error: Error) {
        this.error = error
    }

    constructor(error: Error, message: String?) : super(message) {
        this.error = error
    }

    override fun toString() = super.toString() + "\n" + error.toString()

    enum class Error {
        IO_ERROR,
        UNKNOWN_ERROR
    }
}