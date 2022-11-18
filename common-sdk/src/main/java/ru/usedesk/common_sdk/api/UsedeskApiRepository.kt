package ru.usedesk.common_sdk.api

import com.google.gson.Gson
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
    private val gson: Gson,
    private val apiClass: Class<API>
) {

    protected fun <RESPONSE> doRequest(
        urlApi: String,
        responseClass: Class<RESPONSE>,
        getCall: API.() -> Call<ResponseBody>
    ): RESPONSE = execute(responseClass) {
        apiFactory.getInstance(urlApi, apiClass).getCall()
    }

    protected fun <REQUEST, RESPONSE : UsedeskApiError> doRequestJson(
        urlApi: String,
        body: REQUEST,
        responseClass: Class<RESPONSE>,
        getCall: API.(REQUEST) -> Call<ResponseBody>
    ): RESPONSE? {
        UsedeskLog.onLog("jsonBody") { gson.toJson(body) }
        return executeSafe(
            urlApi,
            responseClass
        ) { getCall(body) }
    }

    protected fun <RESPONSE : UsedeskApiError> doRequestMultipart(
        urlApi: String,
        parts: List<Pair<String, Any?>>,
        responseClass: Class<RESPONSE>,
        apiMethod: API.(parts: List<MultipartBody.Part>) -> Call<ResponseBody>
    ): RESPONSE? {
        UsedeskLog.onLog("multipartBody") { gson.toJson(parts) }
        val multipartParts = parts.mapNotNull(multipartConverter::convert)
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
        UsedeskLog.onLog("rawResponseBody") { rawResponseBody ?: "null" }

        return try {
            gson.fromJson(rawResponseBody, tClass)
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

    companion object {
        private const val MAX_ATTEMPTS = 3

        fun <T> valueOrNull(getValue: () -> T): T? = try {
            getValue()
        } catch (e: Exception) {
            null
        }
    }
}