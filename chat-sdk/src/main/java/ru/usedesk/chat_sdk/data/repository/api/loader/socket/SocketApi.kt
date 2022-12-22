package ru.usedesk.chat_sdk.data.repository.api.loader.socket

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.SocketSendResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException
import javax.inject.Inject

internal class SocketApi @Inject constructor(
    private val gson: Gson,
    private val usedeskOkHttpClientFactory: UsedeskOkHttpClientFactory
) {
    private var socketConnection: SocketConnection? = null
    private val mutex = Mutex()

    fun isConnected() = socketConnection?.isConnected() == true

    fun connect(
        url: String,
        initChatRequest: SocketRequest.Init,
        eventListener: EventListener
    ) {
        runBlocking {
            mutex.withLock {
                try {
                    if (!isConnected()) {
                        socketConnection = SocketConnection(
                            gson,
                            url,
                            usedeskOkHttpClientFactory,
                            initChatRequest,
                            eventListener
                        )
                    }
                } catch (e: Exception) {
                    throw UsedeskSocketException(
                        UsedeskSocketException.Error.SOCKET_INIT_ERROR,
                        e.message
                    )
                }
            }
        }
    }

    fun sendRequest(socketRequest: SocketRequest): SocketSendResponse = try {
        runBlocking {
            mutex.withLock {
                when (socketConnection?.isConnected()) {
                    true -> socketConnection
                    else -> null
                }
            }
        }?.sendRequest(socketRequest)
            ?: throw UsedeskSocketException()
        SocketSendResponse.Done
    } catch (e: Exception) {
        SocketSendResponse.Error
    }

    suspend fun disconnect() {
        mutex.withLock {
            socketConnection?.disconnect()
            socketConnection = null
        }
    }

    interface EventListener {
        fun onConnected()
        fun onDisconnected()
        fun onTokenError()
        fun onFeedback()
        fun onException(exception: Exception)
        fun onInited(initChatResponse: SocketResponse.Inited)
        fun onNew(messageResponse: SocketResponse.AddMessage)
        fun onSetEmailSuccess()
    }
}