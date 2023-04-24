
package ru.usedesk.common_sdk.api

import android.content.Context
import android.os.Build
import com.google.android.gms.security.ProviderInstaller
import okhttp3.OkHttpClient
import javax.inject.Inject

internal class UsedeskOkHttpClientFactory @Inject constructor(
    private val appContext: Context
) : IUsedeskOkHttpClientFactory {
    override fun createInstance(): OkHttpClient = OkHttpClient.Builder().apply {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            try {
                ProviderInstaller.installIfNeeded(appContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }.build()
}