package ru.usedesk.knowledgebase_sdk.data.framework.retrofit

import android.util.Log
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import ru.usedesk.common_sdk.api.IUsedeskApiFactory
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.entity.ApiError

abstract class UsedeskApiRepository<API>(
        private val apiFactory: IUsedeskApiFactory,
        private val gson: Gson,
        private val apiClass: Class<API>
) {

    protected fun <RESPONSE> doRequest(
            responseClass: Class<RESPONSE>,
            getCall: (API) -> Call<ResponseBody>
    ): RESPONSE {
        return execute(gson, responseClass) {
            getCall(apiFactory.getInstance(SERVER_BASE_URL, apiClass))
        }
    }

    private fun <RESPONSE> execute(gson: Gson,
                                   tClass: Class<RESPONSE>,
                                   onGetCall: () -> Call<ResponseBody>): RESPONSE {
        return try {
            val response = (0 until MAX_ATTEMPTS).asSequence().map { attempt ->
                if (attempt != 0) {
                    Thread.sleep(200)
                }
                onGetCall().execute()
            }.filter {
                val filter = it.isSuccessful && it.code() == 200 && it.body() != null
                if (!filter) {
                    Log.d("Response", "failed:\nsuccessful:\n${it.isSuccessful}\ncode:\n${it.code()}\nbody\n${it.body()}")
                }
                filter
            }.firstOrNull() ?: throw UsedeskHttpException()

            val rawResponseBody = response.body()?.string() ?: ""
            try {
                val errorResponse = gson.fromJson(rawResponseBody, ApiError::class.java)
                val code = errorResponse.code
                if (code != null && errorResponse.error != null) {
                    throw UsedeskHttpException(errorResponse.error)
                }
            } catch (e: java.lang.Exception) {
                //nothing
            }
            gson.fromJson(rawResponseBody, tClass)
        } catch (e: Exception) {
            e.printStackTrace()
            throw UsedeskHttpException()
        }
    }

    companion object {
        private const val SERVER_BASE_URL = "https://api.usedesk.ru/support/"
        private const val MAX_ATTEMPTS = 3

        private const val SERVER_ERROR = "111"
        private const val INVALID_TOKEN = "112"
        private const val ACCESS_ERROR = "115"

        fun <T> valueOrNull(lambda: () -> T): T? {
            return try {
                return lambda()
            } catch (e: Exception) {
                null
            }
        }
    }
}