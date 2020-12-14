package ru.usedesk.chat_sdk.data.repository.api.loader.socket

import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class TlsSslSocketFactory constructor(
        private val internalSSLSocketFactory: SSLSocketFactory
) : SSLSocketFactory() {

    private val protocols = arrayOf("TLSv1.2", "TLSv1.1")

    override fun getDefaultCipherSuites(): Array<String> = internalSSLSocketFactory.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = internalSSLSocketFactory.supportedCipherSuites

    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean) =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose))

    override fun createSocket(host: String, port: Int) =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))

    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int) =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort))

    override fun createSocket(host: InetAddress, port: Int) =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port))

    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int) =
            enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort))

    private fun enableTLSOnSocket(socket: Socket?) = socket?.apply {
        if (this is SSLSocket && isTLSServerEnabled(this)) {
            enabledProtocols = protocols
        }
    }

    private fun isTLSServerEnabled(sslSocket: SSLSocket) = sslSocket.supportedProtocols.any { it in protocols }

}