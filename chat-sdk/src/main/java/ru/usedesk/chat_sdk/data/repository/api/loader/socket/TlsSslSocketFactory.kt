package ru.usedesk.chat_sdk.data.repository.api.loader.socket

import okhttp3.TlsVersion
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class TlsSslSocketFactory constructor(
        private val internalSSLSocketFactory: SSLSocketFactory
) : SSLSocketFactory() {

    private val protocols = arrayOf(TlsVersion.TLS_1_2.javaName())

    override fun getDefaultCipherSuites(): Array<String> = internalSSLSocketFactory.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = internalSSLSocketFactory.supportedCipherSuites

    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket? {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose))
    }

    override fun createSocket(host: String, port: Int): Socket? {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))
    }

    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket? {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort))
    }

    override fun createSocket(host: InetAddress, port: Int): Socket? {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))
    }

    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket? {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort))
    }

    private fun enableTLSOnSocket(socket: Socket?): Socket? {
        return socket?.also {
            if (it is SSLSocket) {
                it.enabledProtocols = protocols
            }
        }
    }
}