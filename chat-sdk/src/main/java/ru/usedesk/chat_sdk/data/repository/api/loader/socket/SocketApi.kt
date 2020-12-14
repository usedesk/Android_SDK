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
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException
import toothpick.InjectConstructor
import java.net.HttpURLConnection
import java.net.URISyntaxException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager


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
        it.getOrNull(0)?.apply {
            if (it is Throwable) {
                it.printStackTrace()
            }
        }
        eventListener.onException(UsedeskSocketException(UsedeskSocketException.Error.DISCONNECTED))//TODO:https://coderoad.ru/28943660/%D0%9A%D0%B0%D0%BA-%D0%B2%D0%BA%D0%BB%D1%8E%D1%87%D0%B8%D1%82%D1%8C-%D0%BF%D0%BE%D0%B4%D0%B4%D0%B5%D1%80%D0%B6%D0%BA%D1%83-TLS-1-2-%D0%B2-%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B8-Android-%D1%80%D0%B0%D0%B1%D0%BE%D1%82%D0%B0%D1%8E%D1%89%D0%B5%D0%BC-%D0%BD%D0%B0-Android-4-1
    }

    private val connectEmitterListener = Emitter.Listener {
        eventListener.onConnected()
        sendRequest(initChatRequest)
    }

    private val baseEventEmitterListener = Emitter.Listener {
        onResponse(it[0].toString())
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
                    InitChatResponse.TYPE -> {
                        eventListener.onInited(response as InitChatResponse)
                    }
                    SetEmailResponse.TYPE -> {
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
        if (socket != null) {
            return
        }
        socket = try {
            System.setProperty("https.protocols", "TLSv1.1,TLSv1.2")

            HttpsURLConnection.setDefaultHostnameVerifier { hostname, session ->
                true
            }

            val sslContext = SSLContext.getInstance("TLS").apply {
                init(null, arrayOf<X509TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate?>?,
                                                    authType: String?) {
                    }

                    override fun checkServerTrusted(chain: Array<X509Certificate?>?,
                                                    authType: String?) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }), SecureRandom())

                HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory)
                HttpsURLConnection.setDefaultHostnameVerifier { hostname, session ->
                    true
                }
            }

            val options = IO.Options().apply {
                transports = arrayOf(WebSocket.NAME)
            }

            IO.socket(url, options)
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
    }
}