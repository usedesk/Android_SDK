package ru.usedesk.common_sdk.external.entity.exceptions

abstract class UsedeskException : RuntimeException {

    constructor()

    constructor(message: String?) : super(message)

    override fun toString() = javaClass.name
}