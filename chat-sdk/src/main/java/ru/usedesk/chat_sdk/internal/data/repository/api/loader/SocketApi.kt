package ru.usedesk.chat_sdk.internal.data.repository.api.loader

import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.request.BaseRequest
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.request.InitChatRequest
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.response.*
import ru.usedesk.chat_sdk.internal.data.repository.api._entity.response.ChatInitedResponse
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskSocketException
import toothpick.InjectConstructor
import java.net.HttpURLConnection
import java.net.URISyntaxException

@InjectConstructor
internal class SocketApi(
        private val gson: Gson
) {

    private val emitterListeners: MutableMap<String, Emitter.Listener> = hashMapOf()
    private var socket: Socket? = null

    private lateinit var eventListener: EventListener
    private lateinit var initChatRequest: InitChatRequest

    private val disconnectEmitterListener = Emitter.Listener {
        eventListener.onDisconnected()
    }

    private val connectErrorEmitterListener = Emitter.Listener {
        onConnectError()
    }

    private val connectEmitterListener = Emitter.Listener {
        eventListener.onConnected()
        sendRequest(initChatRequest)
    }

    private val baseEventEmitterListener = Emitter.Listener {
        onResponse(it[0].toString())
    }

    private fun onConnectError() {
        eventListener.onException(UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED))
    }

    private fun onResponse(rawResponse: String) {
        val response = process(rawResponse)
        if (response != null) {
            try {
                when (response.type) {
                    ErrorResponse.TYPE -> {
                        val errorResponse = response as ErrorResponse
                        val usedeskSocketException: UsedeskSocketException
                        usedeskSocketException = when (errorResponse.code) {
                            HttpURLConnection.HTTP_FORBIDDEN -> {
                                eventListener.onTokenError()
                                UsedeskSocketException(UsedeskSocketException.Error.FORBIDDEN_ERROR, errorResponse.message)
                            }
                            HttpURLConnection.HTTP_BAD_REQUEST -> UsedeskSocketException(UsedeskSocketException.Error.BAD_REQUEST_ERROR, errorResponse.message)
                            HttpURLConnection.HTTP_INTERNAL_ERROR -> UsedeskSocketException(UsedeskSocketException.Error.INTERNAL_SERVER_ERROR, errorResponse.message)
                            else -> UsedeskSocketException(UsedeskSocketException.Error.UNKNOWN_FROM_SERVER_ERROR, errorResponse.message)
                        }
                        eventListener.onException(usedeskSocketException)
                    }
                    ChatInitedResponse.TYPE -> {
                        eventListener.onInited(response as ChatInitedResponse)
                    }
                    EmailResponse.TYPE -> {
                    }
                    MessageResponse.TYPE -> {
                        eventListener.onNew(response as MessageResponse)
                    }
                    FeedbackResponse.TYPE -> eventListener.onFeedback()
                }
            } catch (e: Exception) {
                eventListener.onException(e)
            }
        }
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    @Throws(UsedeskException::class)
    fun connect(url: String,
                token: String?,
                companyId: String,
                eventListener: EventListener
    ) {
        if (socket != null) {
            return
        }
        socket = try {
            IO.socket(url)
        } catch (e: URISyntaxException) {
            throw UsedeskSocketException(UsedeskSocketException.Error.IO_ERROR, e.message)
        }

        this.eventListener = eventListener
        this.initChatRequest = InitChatRequest(token, companyId, url)

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
            when (baseRequest.type) {
                ChatInitedResponse.TYPE -> ChatInitedResponse::class.java
                ErrorResponse.TYPE -> ErrorResponse::class.java
                MessageResponse.TYPE -> MessageResponse::class.java
                FeedbackResponse.TYPE -> FeedbackResponse::class.java
                EmailResponse.TYPE -> EmailResponse::class.java
                else -> null
            }?.also {
                return gson.fromJson(rawResponse, it)
            }
        } catch (e: JsonParseException) {
            eventListener.onException(UsedeskSocketException(UsedeskSocketException.Error.JSON_ERROR, e.message))
        }
        return null
    }

    companion object {
        private const val EVENT_SERVER_ACTION = "dispatch"
    }

    interface EventListener {
        fun onConnected()
        fun onDisconnected()
        fun onTokenError()
        fun onFeedback()
        fun onException(exception: Exception)

        fun onInited(chatInitedResponse: ChatInitedResponse)
        fun onNew(messageResponse: MessageResponse)
    }
}