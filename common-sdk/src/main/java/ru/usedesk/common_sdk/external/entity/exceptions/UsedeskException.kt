package ru.usedesk.common_sdk.external.entity.exceptions

abstract class UsedeskException : Exception {

    constructor()

    constructor(message: String?) : super(message)

    override fun toString() = javaClass.name
}