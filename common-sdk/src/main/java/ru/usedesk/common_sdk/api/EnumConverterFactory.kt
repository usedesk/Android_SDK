package ru.usedesk.common_sdk.api

import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class EnumConverterFactory : Converter.Factory() {
    override fun stringConverter(type: Type,
                                 annotations: Array<Annotation>,
                                 retrofit: Retrofit): Converter<*, String>? {
        return if (type is Class<*> && type.isEnum) {
            Converter<Any, String> {
                it.toString().toLowerCase()
            }
        } else null
    }
}