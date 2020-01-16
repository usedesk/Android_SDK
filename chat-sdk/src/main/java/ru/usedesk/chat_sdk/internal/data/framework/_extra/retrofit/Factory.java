package ru.usedesk.chat_sdk.internal.data.framework._extra.retrofit;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

abstract class Factory<K, T> {
    private final Map<K, T> instanceMap = new HashMap<>();

    @NonNull
    public final T getInstance(@NonNull K key) {
        T instance = instanceMap.get(key);
        if (instance == null) {
            instance = createInstance(key);
            instanceMap.put(key, instance);
        }
        return instance;
    }

    protected abstract T createInstance(@NonNull K key);
}
