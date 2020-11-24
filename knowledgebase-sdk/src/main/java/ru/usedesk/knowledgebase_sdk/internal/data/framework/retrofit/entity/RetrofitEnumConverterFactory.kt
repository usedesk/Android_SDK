package ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.entity

import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class RetrofitEnumConverterFactory : Converter.Factory() {
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