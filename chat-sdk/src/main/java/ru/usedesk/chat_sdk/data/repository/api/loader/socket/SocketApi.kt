
package ru.usedesk.chat_sdk.data.repository.api.loader.socket

import com.google.gson.Gson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.usedesk.chat_sdk.data.repository.api.IApiRepository.SocketSendResponse
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketRequest
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse
import ru.usedesk.common_sdk.api.IUsedeskOkHttpClientFactory
import ru.usedesk.common_sdk.entity.exceptions.UsedeskSocketException
import javax.inject.Inject

internal class SocketApi @Inject constructor(
    private val gson: Gson,
    private val usedeskOkHttpClientFactory: IUsedeskOkHttpClientFactory
) {
    private var socketConnection: SocketConnection? = null
    private val mutex = Mutex()

    fun isConnected() = socketConnection?.isConnected() == true

    suspend fun connect(
        url: String,
        initChatRequest: SocketRequest.Init,
        eventListener: EventListener
    ) {
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

    suspend fun sendRequest(socketRequest: SocketRequest): SocketSendResponse = try {
        val connection = mutex.withLock {
            when (socketConnection?.isConnected()) {
                true -> socketConnection
                else -> null
            }
        }
        connection?.sendRequest(socketRequest) ?: throw RuntimeException("No connection")
        SocketSendResponse.Done
    } catch (e: Exception) {
        e.printStackTrace()
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