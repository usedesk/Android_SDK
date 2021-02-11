package ru.usedesk.knowledgebase_gui.entity

class DataOrMessage<T> {
    var data: T? = null
        private set
    var message: Message
        private set

    constructor(data: T) {
        this.data = data
        message = Message.NONE
    }

    constructor(messageState: Message) {
        message = messageState
    }

    enum class Message {
        LOADING, ERROR, NONE
    }
}