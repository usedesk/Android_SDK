package ru.usedesk.common_sdk.internal.api;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class UsedeskApiFactory implements IUsedeskApiFactory {

    private final Gson gson;
    private final Map<String, Object> instanceMap = new HashMap<>();
    private final UsedeskOkHttpClientFactory okHttpClientFactory;

    @Inject
    public UsedeskApiFactory(@NonNull Gson gson,
                             @NonNull UsedeskOkHttpClientFactory okHttpClientFactory) {
        this.gson = gson;
        this.okHttpClientFactory = okHttpClientFactory;
    }

    @Override
    public <API> API getInstance(String baseUrl, Class<API> apiClass) {
        String key = apiClass.getName() + ":" + baseUrl;
        Object instance = instanceMap.get(key);
        if (instance == null) {
            instance = createInstance(baseUrl, apiClass);
            instanceMap.put(key, instance);
        }
        return (API) instance;
    }

    private <API> API createInstance(String baseUrl, Class<API> apiClass) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClientFactory.createInstance())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(apiClass);
    }
}
