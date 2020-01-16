package ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import ru.usedesk.chat_sdk.internal.data.framework.httpapi.IHttpApi;

public class HttpApiFactory extends Factory<String, IHttpApi> implements IHttpApiFactory {
    private final Gson gson;

    @Inject
    HttpApiFactory(@NonNull Gson gson) {
        this.gson = gson;
    }

    @Override
    protected IHttpApi createInstance(@NonNull String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(IHttpApi.class);
    }
}
