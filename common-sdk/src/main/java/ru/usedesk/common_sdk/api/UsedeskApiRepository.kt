
package ru.usedesk.common_sdk.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import ru.usedesk.common_sdk.UsedeskLog
import ru.usedesk.common_sdk.api.entity.UsedeskApiError
import ru.usedesk.common_sdk.api.multipart.IUsedeskMultipartConverter
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException

abstract class UsedeskApiRepository<API>(
    private val apiFactory: IUsedeskApiFactory,
    private val multipartConverter: IUsedeskMultipartConverter,
    protected val gson: Gson,
    private val apiClass: Class<API>
) {

    protected fun <RESPONSE> doRequest(
        urlApi: String,
        responseClass: Class<RESPONSE>,
        getCall: API.() -> Call<ResponseBody>
    ): RESPONSE = execute(responseClass) {
        apiFactory.getInstance(urlApi, apiClass).getCall()
    }

    protected fun <REQUEST : Any, RESPONSE : UsedeskApiError> doRequestJson(
        urlApi: String,
        body: REQUEST,
        responseClass: Class<RESPONSE>,
        getCall: API.(REQUEST) -> Call<ResponseBody>
    ): RESPONSE? {
        UsedeskLog.onLog("jsonBody") { "${body::class.java}\n" + gson.toJson(body) }
        return executeSafe(
            urlApi,
            responseClass
        ) { getCall(body) }
    }

    protected fun <REQUEST : JsonRequest, RESPONSE : UsedeskApiError> doRequestJsonObject(
        urlApi: String,
        body: REQUEST,
        responseClass: Class<RESPONSE>,
        getCall: API.(JsonObject) -> Call<ResponseBody>
    ): RESPONSE? {
        val jsonObject = (gson.toJsonTree(body) as JsonObject).apply {
            body.jsonFields.forEach {
                if (it.second != null) {
                    addProperty(it.first, it.second)
                }
            }
        }
        UsedeskLog.onLog("jsonBody") { "${body::class.java}\n" + gson.toJson(jsonObject) }
        return executeSafe(
            urlApi,
            responseClass
        ) { getCall(jsonObject) }
    }

    protected fun <REQUEST : MultipartRequest, RESPONSE : UsedeskApiError> doRequestMultipart(
        urlApi: String,
        request: REQUEST,
        responseClass: Class<RESPONSE>,
        apiMethod: API.(parts: List<MultipartBody.Part>) -> Call<ResponseBody>,
        progressFlow: MutableStateFlow<Pair<Long, Long>>? = null
    ): RESPONSE? {
        UsedeskLog.onLog("multipartBody") { "${request::class.java}\n" + gson.toJson(request.parts) }
        val multipartParts =
            request.parts.mapNotNull { multipartConverter.convert(it, progressFlow) }
        return executeSafe(
            urlApi,
            responseClass
        ) { apiMethod(multipartParts) }
    }

    private fun <RESPONSE : UsedeskApiError> executeSafe(
        urlApi: String,
        tClass: Class<RESPONSE>,
        onGetCall: API.() -> Call<ResponseBody>
    ): RESPONSE? {
        val rawResponseBody = try {
            val api = apiFactory.getInstance(urlApi, apiClass)
            val response = (0 until MAX_ATTEMPTS).asSequence().map { attempt ->
                if (attempt != 0) {
                    Thread.sleep(200)
                }
                api.onGetCall().execute()
            }.filter {
                val filter = it.isSuccessful && it.code() == 200 && it.body() != null
                if (!filter) {
                    UsedeskLog.onLog("API") {
                        "ResponseFailed:\nsuccessful:\n${it.isSuccessful}\ncode:\n${it.code()}\nbody\n${it.body()}"
                    }
                }
                filter
            }.firstOrNull()

            response?.body()?.string()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        UsedeskLog.onLog("rawResponseBody") { "$tClass\n" + (rawResponseBody ?: "null") }

        return try {
            val safeRawResponse = when {
                rawResponseBody == "" -> "{}"
                rawResponseBody?.startsWith("[") == true -> """{"items":$rawResponseBody}"""
                else -> rawResponseBody
            }
            gson.fromJson(safeRawResponse, tClass)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Deprecated("Call executeSafe instead")
    private fun <RESPONSE> execute(
        tClass: Class<RESPONSE>,
        onGetCall: () -> Call<ResponseBody>
    ): RESPONSE {
        var rawResponseBody = ""
        return try {
            val response = (0 until MAX_ATTEMPTS).asSequence().map { attempt ->
                if (attempt != 0) {
                    Thread.sleep(200)
                }
                onGetCall().execute()
            }.filter {
                val filter = it.isSuccessful && it.code() == 200 && it.body() != null
                if (!filter) {
                    UsedeskLog.onLog("API") {
                        "ResponseFailed:\nsuccessful:\n${it.isSuccessful}\ncode:\n${it.code()}\nbody\n${it.body()}"
                    }
                }
                filter
            }.firstOrNull() ?: throw UsedeskHttpException(message = "Failed to get a response")

            rawResponseBody = response.body()?.string() ?: ""
            UsedeskLog.onLog("RESP") { rawResponseBody }
            val errorResponse = try {
                gson.fromJson(rawResponseBody, UsedeskApiError::class.java)
            } catch (e: Exception) {
                null
            }
            if (errorResponse?.error != null && errorResponse.code != null) {
                throw UsedeskHttpException(message = errorResponse.error)
            }
            gson.fromJson(rawResponseBody, tClass)
        } catch (e: Exception) {
            if (rawResponseBody.isNotEmpty()) {
                UsedeskLog.onLog("API") { "Failed to parse the response: $rawResponseBody" }
            }
            e.printStackTrace()
            throw when (e) {
                is UsedeskHttpException -> e
                else -> UsedeskHttpException()
            }
        }
    }

    abstract class MultipartRequest(vararg val parts: Pair<String, Any?>)

    abstract class JsonRequest(
        @Expose(serialize = false)
        val jsonFields: List<Pair<String, String?>> = listOf()
    )

    companion object {
        private const val MAX_ATTEMPTS = 3

        fun <T> valueOrNull(getValue: () -> T): T? = try {
            getValue()
        } catch (e: Exception) {
            null
        }
    }
}