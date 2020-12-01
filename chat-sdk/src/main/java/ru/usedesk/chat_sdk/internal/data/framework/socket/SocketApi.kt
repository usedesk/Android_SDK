package ru.usedesk.chat_sdk.internal.data.framework.socket

import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import ru.usedesk.chat_sdk.external.entity.UsedeskChatConfiguration
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage
import ru.usedesk.chat_sdk.external.entity.chat.*
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.BaseRequest
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.InitChatRequest
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.*
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.init.InitChatResponse
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.init.InitedChat
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException
import toothpick.InjectConstructor
import java.net.HttpURLConnection
import java.net.URISyntaxException
import java.util.*

@InjectConstructor
class SocketApi(
        private val gson: Gson
) {

    private val emitterListeners: MutableMap<String, Emitter.Listener> = hashMapOf()
    private var socket: Socket? = null

    private lateinit var configuration: UsedeskChatConfiguration
    private lateinit var token: String

    private lateinit var onDisconnected: () -> Unit
    private lateinit var onTokenError: () -> Unit
    private lateinit var onInited: (InitChatResponse) -> Unit
    private lateinit var onNew: (UsedeskMessage) -> Unit
    private lateinit var onFeedback: () -> Unit
    private lateinit var onException: (Exception) -> Unit

    private val disconnectEmitterListener = Emitter.Listener {
        onDisconnected()
    }

    private val connectErrorEmitterListener = Emitter.Listener {
        onConnectError()
    }

    private val connectEmitterListener = Emitter.Listener {
        onInitChat()
    }

    private val baseEventEmitterListener = Emitter.Listener {
        onResponse(it[0].toString())
    }

    private fun onConnectError() {
        onException(UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED))
    }

    private fun onInitChat() {
        sendRequest(InitChatRequest(token,
                configuration.companyId,
                configuration.url))
    }

    private fun convert(initChatResponse: InitChatResponse): InitedChat? {
        return try {
            InitedChat(
                    initChatResponse.token!!,
                    initChatResponse.noOperators!!,
                    initChatResponse.setup!!.waitingEmail!!,
                    convert(initChatResponse.setup.messages ?: listOf())
            )
        } catch (e: Exception) {
            onException(e)
            null
        }
    }

    private fun convert(messages: List<InitChatResponse.Setup.Message?>): List<UsedeskChatItem> {
        return messages.flatMap {
            try {
                convert(it?.payload)
            } catch (e: Exception) {
                listOf()
            }
        }
    }

    private fun convert(message: InitChatResponse.Setup.Message.Payload?): List<UsedeskChatItem> {
        val fromClient: Boolean = when (message!!.type) {
            InitChatResponse.Setup.Message.Payload.Type.CLIENT_TO_OPERATOR,
            InitChatResponse.Setup.Message.Payload.Type.CLIENT_TO_BOT -> {
                true
            }
            InitChatResponse.Setup.Message.Payload.Type.OPERATOR_TO_CLIENT,
            InitChatResponse.Setup.Message.Payload.Type.BOT_TO_CLIENT -> {
                false
            }
            else -> {
                return listOf()
            }
        }
        val messageDate = Calendar.getInstance()

        if (message.file != null) {
            val file = UsedeskFile(message.file.content!!,
                    message.file.type!!,
                    message.file.size!!,
                    message.file.name!!)

            if (file.isImage()) {
                if (fromClient) {
                    UsedeskMessageClientImage(messageDate, file)
                } else {
                    UsedeskMessageAgentImage(messageDate,
                            file,
                            message.name ?: "",
                            message.usedeskPayload?.avatar ?: "")
                }
            } else {
                if (fromClient) {
                    UsedeskMessageClientFile(messageDate,
                            file)
                } else {
                    UsedeskMessageAgentFile(messageDate,
                            file,
                            message.name ?: "",
                            message.usedeskPayload?.avatar ?: "")
                }
            }
        }

        if (message.text?.isNotEmpty() == true) {
            val text: String
            val html: String

            val divIndex = message.text.indexOf("<div")

            if (divIndex >= 0) {
                text = message.text.substring(0, divIndex)

                html = message.text.removePrefix(text)
            } else {
                text = message.text
                html = ""
            }

            val convertedText = text
                    .replace("<strong data-verified=\"redactor\" data-redactor-tag=\"strong\">", "<b>")
                    .replace("</strong>", "</b>")
                    .replace("<em data-verified=\"redactor\" data-redactor-tag=\"em\">", "<i>")
                    .replace("</em>", "</i>")
                    .replace("</p>", "")
                    .removePrefix("<p>")
                    .trim()

            if (text.isEmpty() && html.isEmpty()) {
                null
            } else if (fromClient) {
                UsedeskMessageClientText(messageDate,
                        convertedText,
                        html)
            } else {
                UsedeskMessageAgentText(messageDate,
                        convertedText,
                        html,
                        message.name ?: "",
                        message.usedeskPayload?.avatar ?: "")
            }
        }
    }

    private fun onResponse(rawResponse: String) {
        val response = process(rawResponse)
        if (response != null) {
            when (response.type) {
                ErrorResponse.TYPE -> {
                    val errorResponse = response as ErrorResponse
                    val usedeskSocketException: UsedeskSocketException
                    usedeskSocketException = when (errorResponse.code) {
                        HttpURLConnection.HTTP_FORBIDDEN -> {
                            onTokenError()
                            UsedeskSocketException(UsedeskSocketException.Error.FORBIDDEN_ERROR, errorResponse.message)
                        }
                        HttpURLConnection.HTTP_BAD_REQUEST -> UsedeskSocketException(UsedeskSocketException.Error.BAD_REQUEST_ERROR, errorResponse.message)
                        HttpURLConnection.HTTP_INTERNAL_ERROR -> UsedeskSocketException(UsedeskSocketException.Error.INTERNAL_SERVER_ERROR, errorResponse.message)
                        else -> UsedeskSocketException(UsedeskSocketException.Error.UNKNOWN_FROM_SERVER_ERROR, errorResponse.message)
                    }
                    onException(usedeskSocketException)
                }
                InitChatResponse.TYPE -> {
                    onInited(response as InitChatResponse)
                }
                SetEmailResponse.TYPE -> {
                }
                NewMessageResponse.TYPE -> {
                    val newMessageResponse = response as NewMessageResponse
                    onNew(newMessageResponse.message)
                }
                SendFeedbackResponse.TYPE -> onFeedback()
            }
        }
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    @Throws(UsedeskException::class)
    fun connect(url: String,
                onDisconnected: () -> Unit,
                onTokenError: () -> Unit,
                onInit: (InitChatResponse) -> Unit,
                onNew: (UsedeskMessage) -> Unit,
                onFeedback: () -> Unit,
                onException: (Exception) -> Unit
    ) {
        if (socket != null) {
            return
        }
        socket = try {
            IO.socket(url)
        } catch (e: URISyntaxException) {
            throw UsedeskSocketException(UsedeskSocketException.Error.IO_ERROR, e.message)
        }

        this.onDisconnected = onDisconnected
        this.onTokenError = onTokenError
        this.onInited = onInited
        this.onNew = onNew
        this.onFeedback = onFeedback
        this.onException = onException

        emitterListeners[EVENT_SERVER_ACTION] = baseEventEmitterListener
        emitterListeners[Socket.EVENT_CONNECT_ERROR] = connectErrorEmitterListener
        emitterListeners[Socket.EVENT_CONNECT_TIMEOUT] = connectErrorEmitterListener
        emitterListeners[Socket.EVENT_DISCONNECT] = disconnectEmitterListener
        emitterListeners[Socket.EVENT_CONNECT] = connectEmitterListener

        for (event in emitterListeners.keys) {
            socket?.on(event, emitterListeners[event])
        }
        socket?.connect()
    }

    fun disconnect() {
        for (event in emitterListeners.keys) {
            socket?.off(event, emitterListeners[event])
        }
        emitterListeners.clear()
        socket?.disconnect()
    }

    @Throws(UsedeskSocketException::class)
    fun sendRequest(baseRequest: BaseRequest) {
        try {
            val jsonObject = JSONObject(gson.toJson(baseRequest))
            socket?.emit(EVENT_SERVER_ACTION, jsonObject)
        } catch (e: JSONException) {
            throw UsedeskSocketException(UsedeskSocketException.Error.JSON_ERROR, e.message)
        }
    }

    private fun process(rawResponse: String): BaseResponse? {
        try {
            val baseRequest = gson.fromJson(rawResponse, BaseRequest::class.java)
            if (baseRequest?.type != null) {
                when (baseRequest.type) {
                    InitChatResponse.TYPE -> return gson.fromJson(rawResponse, InitChatResponse::class.java)
                    ErrorResponse.TYPE -> return gson.fromJson(rawResponse, ErrorResponse::class.java)
                    NewMessageResponse.TYPE -> return NewMessageResponse(getMessage(rawResponse))
                    SendFeedbackResponse.TYPE -> return gson.fromJson(rawResponse, SendFeedbackResponse::class.java)
                    SetEmailResponse.TYPE -> return gson.fromJson(rawResponse, SetEmailResponse::class.java)
                }
            }
        } catch (e: JsonParseException) {
            onException(UsedeskSocketException(UsedeskSocketException.Error.JSON_ERROR, e.message))
        }
        return null
    }

    @Throws(JsonParseException::class)
    private fun getMessage(rawResponse: String): UsedeskMessage {
        return try {
            val payloadMessageResponse = gson.fromJson(rawResponse, PayloadMessageResponse::class.java)//TODO: эту всю дрочь перенести в Response с var? полями, её проверять и из неё уже делать нормальные null-безопасные объекты, с формализованными данными, а не Payload и прочую срань, тьфу...
            val payloadMessage = payloadMessageResponse.message
            UsedeskMessage(payloadMessage!!, payloadMessage.payload, null)
        } catch (e: JsonParseException) {
            val simpleMessageResponse = gson.fromJson(rawResponse, SimpleMessageResponse::class.java)
            val simpleMessage = simpleMessageResponse.message
            UsedeskMessage(simpleMessage!!, null, simpleMessage.payload)
        }
    }

    companion object {
        private const val EVENT_SERVER_ACTION = "dispatch"
    }
}