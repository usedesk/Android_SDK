package ru.usedesk.common_gui

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.LibraryGlideModule
import ru.usedesk.common_sdk.api.UsedeskOkHttpClientFactory
import java.io.InputStream

@GlideModule
class UsedeskGlideModule : LibraryGlideModule() {

    override fun registerComponents(context: Context,
                                    glide: Glide,
                                    registry: Registry) {
        val client = UsedeskOkHttpClientFactory(context).createInstance()
        val factory = OkHttpUrlLoader.Factory(client)
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }
}