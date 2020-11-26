package ru.usedesk.chat_sdk.internal.data.framework.socket

import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import ru.usedesk.chat_sdk.external.entity.IUsedeskActionListener
import ru.usedesk.chat_sdk.external.entity.UsedeskMessage
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.request.BaseRequest
import ru.usedesk.chat_sdk.internal.data.framework.socket.entity.response.*
import ru.usedesk.chat_sdk.internal.domain.entity.OnMessageListener
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException
import toothpick.InjectConstructor
import java.net.HttpURLConnection
import java.net.URISyntaxException

@InjectConstructor
class SocketApi(
        private val gson: Gson
) {

    private val emitterListeners: MutableMap<String, Emitter.Listener> = hashMapOf()
    private var socket: Socket? = null
    private var actionListener: IUsedeskActionListener? = null
    private var onMessageListener: OnMessageListener? = null

    private val disconnectEmitterListener = Emitter.Listener {
        onDisconnected()
    }

    private val connectErrorEmitterListener = Emitter.Listener {
        onConnectError()
    }

    private val connectEmitterListener = Emitter.Listener {
        onConnect()
    }

    private val baseEventEmitterListener = Emitter.Listener {
        onResponse(it[0].toString())
    }

    private fun onDisconnected() {
        actionListener?.onDisconnected()
    }

    private fun onConnectError() {
        actionListener?.onException(UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED))
    }

    private fun onConnect() {
        onMessageListener?.onInitChat()
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
                            onMessageListener?.onTokenError()
                            UsedeskSocketException(UsedeskSocketException.Error.FORBIDDEN_ERROR, errorResponse.message)
                        }
                        HttpURLConnection.HTTP_BAD_REQUEST -> UsedeskSocketException(UsedeskSocketException.Error.BAD_REQUEST_ERROR, errorResponse.message)
                        HttpURLConnection.HTTP_INTERNAL_ERROR -> UsedeskSocketException(UsedeskSocketException.Error.INTERNAL_SERVER_ERROR, errorResponse.message)
                        else -> UsedeskSocketException(UsedeskSocketException.Error.UNKNOWN_FROM_SERVER_ERROR, errorResponse.message)
                    }
                    actionListener?.onException(usedeskSocketException)
                }
                InitChatResponse.TYPE -> {
                    onMessageListener?.onInit(response as InitChatResponse)
                }
                SetEmailResponse.TYPE -> {
                }
                NewMessageResponse.TYPE -> {
                    val newMessageResponse = response as NewMessageResponse
                    onMessageListener?.onNew(newMessageResponse.message)
                }
                SendFeedbackResponse.TYPE -> onMessageListener?.onFeedback()
            }
        }
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    @Throws(UsedeskException::class)
    fun connect(url: String, actionListener: IUsedeskActionListener,
                onMessageListener: OnMessageListener) {
        if (socket != null) {
            return
        }
        socket = try {
            IO.socket(url)
        } catch (e: URISyntaxException) {
            throw UsedeskSocketException(UsedeskSocketException.Error.IO_ERROR, e.message)
        }
        this.actionListener = actionListener
        this.onMessageListener = onMessageListener

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
            actionListener?.onException(UsedeskSocketException(UsedeskSocketException.Error.JSON_ERROR, e.message))
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