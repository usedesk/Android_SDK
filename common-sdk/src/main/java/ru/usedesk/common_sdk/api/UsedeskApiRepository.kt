package ru.usedesk.common_sdk.api

import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import ru.usedesk.common_sdk.UsedeskLog
import ru.usedesk.common_sdk.api.entity.ApiError
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
    ): RESPONSE = execute(gson, responseClass) {
        getCall(apiFactory.getInstance(urlApi, apiClass))
    }

    protected fun <RESPONSE> doRequestMultipart(
        urlApi: String,
        parts: List<Pair<String, Any?>>,
        responseClass: Class<RESPONSE>,
        apiMethod: API.(parts: List<MultipartBody.Part>) -> Call<ResponseBody>
    ): RESPONSE {
        val multipartParts = parts.mapNotNull(multipartConverter::convert)
        return execute(gson, responseClass) {
            apiFactory.getInstance(urlApi, apiClass)
                .apiMethod(multipartParts)
        }
    }

    private fun <RESPONSE> execute(
        gson: Gson,
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
                    UsedeskLog.onLog(
                        "API",
                        "ResponseFailed:\nsuccessful:\n${it.isSuccessful}\ncode:\n${it.code()}\nbody\n${it.body()}"
                    )
                }
                filter
            }.firstOrNull() ?: throw UsedeskHttpException("Failed to get a response")

            //rawResponseBody = "{\"ticket_id\":102937549,\"status\":200}"
            rawResponseBody = response.body()?.string() ?: ""
            UsedeskLog.onLog("RESP", rawResponseBody)
            try {
                val errorResponse = gson.fromJson(rawResponseBody, ApiError::class.java)
                val code = errorResponse.code
                if (code != null && errorResponse.error != null) {
                    throw UsedeskHttpException(errorResponse.error)
                }
            } catch (e: Exception) {
                //nothing
            }
            gson.fromJson(rawResponseBody, tClass)
        } catch (e: Exception) {
            if (rawResponseBody.isNotEmpty()) {
                UsedeskLog.onLog(
                    "API",
                    "Failed to parse the response: $rawResponseBody"
                )
            }
            e.printStackTrace()
            throw UsedeskHttpException()
        }
    }

    companion object {
        private const val MAX_ATTEMPTS = 3

        fun <T> valueOrNull(lambda: () -> T): T? = try {
            lambda()
        } catch (e: Exception) {
            null
        }
    }
}