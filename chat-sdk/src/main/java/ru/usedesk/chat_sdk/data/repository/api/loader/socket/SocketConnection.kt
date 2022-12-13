package ru.usedesk.chat_sdk.data.repository.api.loader.socket

import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
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
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.setemail.SetClientResponse
import ru.usedesk.common_sdk.UsedeskLog
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException
import java.net.HttpURLConnection

internal class SocketConnection(
    private val gson: Gson,
    url: String,
    usedeskOkHttpClientFactory: UsedeskOkHttpClientFactory,
    private val initChatRequest: InitChatRequest,
    private val eventListener: SocketApi.EventListener
) {
    private val socket: Socket

    init {
        usedeskOkHttpClientFactory.createInstance().also {
            IO.setDefaultOkHttpWebSocketFactory(it)
            IO.setDefaultOkHttpCallFactory(it)
        }

        socket = IO.socket(
            url,
            IO.Options().apply { transports = arrayOf(WebSocket.NAME) }
        ).apply {
            on(Socket.EVENT_CONNECT) {
                eventListener.onConnected()
                sendRequest(initChatRequest)
            }
            on(Socket.EVENT_CONNECT_ERROR) {
                (it.getOrNull(0) as? Throwable)?.printStackTrace()
                this@SocketConnection.disconnect()
            }
            on(EVENT_SERVER_ACTION) {
                onResponse(it[0].toString())
            }
            on(Socket.EVENT_DISCONNECT) {
                this@SocketConnection.disconnect()
            }

            connect()
        }
    }

    private fun parse(rawResponse: String) = try {
        val baseResponse = gson.fromJson(rawResponse, BaseResponse::class.java)
        val responseClass = when (baseResponse.type) {
            InitChatResponse.TYPE -> InitChatResponse::class.java
            ErrorResponse.TYPE -> ErrorResponse::class.java
            MessageResponse.TYPE -> MessageResponse::class.java
            FeedbackResponse.TYPE -> FeedbackResponse::class.java
            SetClientResponse.TYPE -> SetClientResponse::class.java
            else -> throw RuntimeException("Could not find response class by type")
        }
        gson.fromJson(rawResponse, responseClass)
    } catch (e: Exception) {
        UsedeskLog.onLog("Socket: Failed to parse the response", rawResponse)
        throw UsedeskSocketException(
            UsedeskSocketException.Error.JSON_ERROR,
            e.message
        )
    }

    private fun onResponse(rawResponse: String) {
        try {
            UsedeskLog.onLog("Socket.rawResponse", rawResponse)
            when (val response = parse(rawResponse)) {
                is ErrorResponse -> {
                    val usedeskSocketException = when (response.code) {
                        HttpURLConnection.HTTP_FORBIDDEN -> {
                            eventListener.onTokenError()
                            UsedeskSocketException(
                                UsedeskSocketException.Error.FORBIDDEN_ERROR,
                                response.message
                            )
                        }
                        HttpURLConnection.HTTP_BAD_REQUEST -> UsedeskSocketException(
                            UsedeskSocketException.Error.BAD_REQUEST_ERROR,
                            response.message
                        )
                        HttpURLConnection.HTTP_INTERNAL_ERROR -> UsedeskSocketException(
                            UsedeskSocketException.Error.INTERNAL_SERVER_ERROR,
                            response.message
                        )
                        else -> UsedeskSocketException(response.message)
                    }
                    eventListener.onException(usedeskSocketException)
                }
                is InitChatResponse -> eventListener.onInited(response)
                is SetClientResponse -> eventListener.onSetEmailSuccess()
                is MessageResponse -> eventListener.onNew(response)
                is FeedbackResponse -> eventListener.onFeedback()
            }
        } catch (e: Exception) {
            eventListener.onException(e)
        }
    }

    fun sendRequest(baseRequest: BaseRequest) {
        try {
            val rawRequest = gson.toJson(baseRequest)
            val jsonRequest = JSONObject(rawRequest)
            UsedeskLog.onLog("Socket.sendRequest", gson.toJson(rawRequest))
            socket.emit(EVENT_SERVER_ACTION, jsonRequest)
        } catch (e: JSONException) {
            throw UsedeskSocketException(UsedeskSocketException.Error.JSON_ERROR, e.message)
        }
    }

    fun isConnected() = socket.connected()

    fun disconnect() {
        socket.run {
            off(Socket.EVENT_CONNECT)
            off(Socket.EVENT_CONNECT_ERROR)
            off(EVENT_SERVER_ACTION)
            off(Socket.EVENT_DISCONNECT)
            disconnect()
            close()
        }
        eventListener.onDisconnected()
    }

    companion object {
        private const val EVENT_SERVER_ACTION = "dispatch"
    }
}