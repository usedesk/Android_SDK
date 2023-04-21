
package ru.usedesk.chat_sdk.data.repository.api.loader.socket

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONObject
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse.AddMessage
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse.ErrorResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse.FeedbackResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse.Inited
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse.SetClient
import ru.usedesk.common_sdk.UsedeskLog
import ru.usedesk.common_sdk.api.IUsedeskOkHttpClientFactory
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException
import java.net.HttpURLConnection

internal class SocketConnection(
    private val gson: Gson,
    url: String,
    usedeskOkHttpClientFactory: IUsedeskOkHttpClientFactory,
    private val initChatRequest: SocketRequest.Init,
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
            val connectTimeStamp = System.currentTimeMillis()
            on(Socket.EVENT_CONNECT_ERROR) {
                (it.getOrNull(0) as? Throwable)?.printStackTrace()
                if (System.currentTimeMillis() - connectTimeStamp > CONNECTION_TIMEOUT_MILLIS) {
                    this@SocketConnection.disconnect()
                }
            }
            on(EVENT_SERVER_ACTION) {
                onResponse(it[0].toString())
            }
            on(Socket.EVENT_DISCONNECT) {
                this@SocketConnection.disconnect()
            }

            open()
        }
    }

    private fun parse(rawResponse: String) = try {
        val jsonObject = gson.fromJson(rawResponse, JsonObject::class.java)
        val responseClass = when (val type = jsonObject.get("type").asString) {
            "@@chat/current/INITED" -> Inited::class.java
            "@@chat/current/ADD_MESSAGE" -> AddMessage::class.java
            "@@chat/current/CALLBACK_ANSWER" -> FeedbackResponse::class.java
            "@@chat/current/SET" -> SetClient::class.java
            "@@redbone/ERROR" -> ErrorResponse::class.java
            else -> throw RuntimeException("""Could not find response class by type: "$type"""")
        }
        gson.fromJson(jsonObject, responseClass)
    } catch (e: Exception) {
        UsedeskLog.onLog("SOCKET") { "Failed to parse the response: $rawResponse" }
        throw UsedeskSocketException(
            UsedeskSocketException.Error.JSON_ERROR,
            e.message
        )
    }

    private fun onResponse(rawResponse: String) {
        try {
            UsedeskLog.onLog("Socket.rawResponse") { rawResponse }
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
                is Inited -> eventListener.onInited(response)
                is SetClient -> eventListener.onSetEmailSuccess()
                is AddMessage -> eventListener.onNew(response)
                is FeedbackResponse -> eventListener.onFeedback() //TODO: Need to add some data in this respose
            }
        } catch (e: Exception) {
            eventListener.onException(e)
        }
    }

    fun sendRequest(socketRequest: SocketRequest) {
        val rawRequest = gson.toJson(socketRequest)
        val jsonRequest = JSONObject(rawRequest)
        UsedeskLog.onLog("Socket.sendRequest") { rawRequest }
        socket.emit(EVENT_SERVER_ACTION, jsonRequest)
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
        private const val CONNECTION_TIMEOUT_MILLIS = 30000
    }
}