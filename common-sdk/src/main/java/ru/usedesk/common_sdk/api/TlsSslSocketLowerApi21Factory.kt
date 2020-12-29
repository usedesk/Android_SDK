package ru.usedesk.common_sdk.api

import okhttp3.TlsVersion
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

internal class TlsSslSocketLowerApi21Factory constructor(
        private val sslSocketFactory: SSLSocketFactory
) : SSLSocketFactory() {

    private val protocols = arrayOf(TlsVersion.TLS_1_2.javaName())

    override fun getDefaultCipherSuites(): Array<String> = sslSocketFactory.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = sslSocketFactory.supportedCipherSuites

    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket? {
        return enableTLSOnSocket(sslSocketFactory.createSocket(s, host, port, autoClose))
    }

    override fun createSocket(host: String, port: Int): Socket? {
        return enableTLSOnSocket(sslSocketFactory.createSocket(host, port))
    }

    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket? {
        return enableTLSOnSocket(sslSocketFactory.createSocket(host, port, localHost, localPort))
    }

    override fun createSocket(host: InetAddress, port: Int): Socket? {
        return enableTLSOnSocket(sslSocketFactory.createSocket(host, port))
    }

    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket? {
        return enableTLSOnSocket(sslSocketFactory.createSocket(address, port, localAddress, localPort))
    }

    private fun enableTLSOnSocket(socket: Socket?): Socket? {
        return socket?.also {
            if (it is SSLSocket) {
                it.enabledProtocols = protocols
            }
        }
    }
}