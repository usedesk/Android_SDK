package ru.usedesk.common_sdk.api

import android.content.Context
import android.os.Build
import com.google.android.gms.security.ProviderInstaller
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import toothpick.InjectConstructor
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

@InjectConstructor
class UsedeskOkHttpClientFactory(
        private val appContext: Context
) {
    fun createInstance(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                try {
                    ProviderInstaller.installIfNeeded(appContext)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val allTrustManager = object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

                    override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
                    }

                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    }
                }

                val sslContext = SSLContext.getInstance(TlsVersion.TLS_1_2.javaName()).apply {
                    init(null, arrayOf(allTrustManager), null)
                }

                val sslSocketFactory = TlsSslSocketLowerApi21Factory(sslContext.socketFactory)

                sslSocketFactory(sslSocketFactory, allTrustManager)
                hostnameVerifier { _, _ ->
                    true
                }
            }
        }.build()
    }
}