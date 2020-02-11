package ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.Converter;
import retrofit2.Retrofit;

public class RetrofitEnumConverterFactory extends Converter.Factory {
    @Override
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (type instanceof Class && ((Class<?>) type).isEnum()) {
            return (Converter<Object, String>) value -> {
                if (value != null) {
                    return value.toString().toLowerCase();
                }
                return null;
            };
        }
        return null;
    }
}