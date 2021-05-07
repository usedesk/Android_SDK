package ru.usedesk.chat_sdk.data.repository.api.loader.socket

import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONException
import org.json.JSONObject
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity._extra.BaseResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.error.ErrorResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.feedback.FeedbackResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.initchat.InitChatResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.setemail.SetEmailResponse
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException
import toothpick.InjectConstructor
import java.net.HttpURLConnection
import java.net.URISyntaxException

@InjectConstructor
internal class SocketApi(
        private val gson: Gson,
        private val usedeskOkHttpClientFactory: UsedeskOkHttpClientFactory
) {

    private val emitterListeners: MutableMap<String, Emitter.Listener> = hashMapOf()
    private var socket: Socket? = null

    private lateinit var eventListener: EventListener
    private lateinit var initChatRequest: InitChatRequest

    private val disconnectEmitterListener = Emitter.Listener {
        eventListener.onDisconnected()
    }

    private val connectErrorEmitterListener = Emitter.Listener {
        it.getOrNull(0)?.also { arg ->
            if (arg is Throwable) {
                arg.printStackTrace()
            }
        }
        eventListener.onException(UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED))
    }

    private val connectEmitterListener = Emitter.Listener {
        eventListener.onConnected()
        sendRequest(initChatRequest)
    }

    private val baseEventEmitterListener = Emitter.Listener {
        onResponse(it[0].toString())
    }

    private fun onResponse(rawResponse: String) {
        //Log.d("RESPONSE", rawResponse)
        val response = process(rawResponse)
        if (response != null) {
            try {
                when (response.type) {
                    ErrorResponse.TYPE -> {
                        val errorResponse = response as ErrorResponse
                        val usedeskSocketException: UsedeskSocketException = when (errorResponse.code) {
                            HttpURLConnection.HTTP_FORBIDDEN -> {
                                eventListener.onTokenError()
                                UsedeskSocketException(UsedeskSocketException.Error.FORBIDDEN_ERROR, errorResponse.message)
                            }
                            HttpURLConnection.HTTP_BAD_REQUEST -> UsedeskSocketException(UsedeskSocketException.Error.BAD_REQUEST_ERROR, errorResponse.message)
                            HttpURLConnection.HTTP_INTERNAL_ERROR -> UsedeskSocketException(UsedeskSocketException.Error.INTERNAL_SERVER_ERROR, errorResponse.message)
                            else -> UsedeskSocketException(errorResponse.message)
                        }
                        eventListener.onException(usedeskSocketException)
                    }
                    InitChatResponse.TYPE -> {
                        eventListener.onInited(response as InitChatResponse)
                    }
                    SetEmailResponse.TYPE -> {
                        eventListener.onSetEmailSuccess()
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

    fun connect(url: String,
                token: String?,
                companyId: String,
                eventListener: EventListener
    ) {
        socket = (socket ?: getSocket(url)).also { socket ->
            this.eventListener = eventListener
            this.initChatRequest = InitChatRequest(token, companyId, url)

            emitterListeners[EVENT_SERVER_ACTION] = baseEventEmitterListener
            emitterListeners[Socket.EVENT_CONNECT_ERROR] = connectErrorEmitterListener
            emitterListeners[Socket.EVENT_CONNECT_TIMEOUT] = connectErrorEmitterListener
            emitterListeners[Socket.EVENT_DISCONNECT] = disconnectEmitterListener
            emitterListeners[Socket.EVENT_CONNECT] = connectEmitterListener

            for (event in emitterListeners.keys) {
                socket.on(event, emitterListeners[event])
            }
            socket.connect()
        }
    }

    private fun getSocket(url: String): Socket {
        return try {
            usedeskOkHttpClientFactory.createInstance().also {
                IO.setDefaultOkHttpWebSocketFactory(it)
                IO.setDefaultOkHttpCallFactory(it)
            }

            val options = IO.Options().apply {
                transports = arrayOf(WebSocket.NAME)
            }

            IO.socket(url, options)
        } catch (e: URISyntaxException) {
            throw UsedeskSocketException(UsedeskSocketException.Error.SOCKET_INIT_ERROR, e.message)
        }
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
            val rawRequest = gson.toJson(baseRequest)
            //Log.d("DBG", baseRequest.javaClass.simpleName + ":" +rawRequest)
            val jsonRequest = JSONObject(rawRequest)
            socket?.emit(EVENT_SERVER_ACTION, jsonRequest)
        } catch (e: JSONException) {
            throw UsedeskSocketException(UsedeskSocketException.Error.JSON_ERROR, e.message)
        }
    }

    private fun process(rawResponse: String): BaseResponse? {
        try {
            val baseRequest = gson.fromJson(rawResponse, BaseRequest::class.java)
            when (baseRequest.type) {
                InitChatResponse.TYPE -> InitChatResponse::class.java
                ErrorResponse.TYPE -> ErrorResponse::class.java
                MessageResponse.TYPE -> MessageResponse::class.java
                FeedbackResponse.TYPE -> FeedbackResponse::class.java
                SetEmailResponse.TYPE -> SetEmailResponse::class.java
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

        fun onInited(initChatResponse: InitChatResponse)
        fun onNew(messageResponse: MessageResponse)
        fun onSetEmailSuccess()
    }
}