package ru.usedesk.common_gui.internal;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.LibraryGlideModule;

import java.io.InputStream;

import okhttp3.OkHttpClient;
import ru.usedesk.common_sdk.internal.api.UsedeskOkHttpClientFactory;

@GlideModule
public class UsedeskGlideModule extends LibraryGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, Glide glide, @NonNull Registry registry) {
        OkHttpClient client = new UsedeskOkHttpClientFactory(context).createInstance();

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);

        glide.getRegistry().replace(GlideUrl.class, InputStream.class, factory);
    }
}
