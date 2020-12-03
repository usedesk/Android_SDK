package ru.usedesk.knowledgebase_sdk.data.framework.retrofit

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import ru.usedesk.common_sdk.entity.exceptions.UsedeskHttpException
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.entity.ApiError
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.entity.ArticlesBodyPage
import ru.usedesk.knowledgebase_sdk.data.framework.retrofit.entity.ViewsAdded
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleBody
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSearchQuery
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection
import toothpick.InjectConstructor
import java.io.IOException
import java.util.*

@InjectConstructor
internal class ApiLoader constructor(
        private val apiRetrofit: ApiRetrofit,
        private val gson: Gson
) : IApiLoader {
    @Throws(UsedeskHttpException::class)
    override fun getSections(accountId: String, token: String): Array<UsedeskSection> {
        return executeRequest(Array<UsedeskSection>::class.java, apiRetrofit.getSections(accountId, token))
    }

    @Throws(UsedeskHttpException::class)
    override fun getArticle(accountId: String, articleId: String,
                            token: String): UsedeskArticleBody {
        return executeRequest(UsedeskArticleBody::class.java, apiRetrofit.getArticleBody(accountId, articleId, token))
    }

    @Throws(UsedeskHttpException::class)
    override fun getArticles(accountId: String, token: String,
                             searchQuery: UsedeskSearchQuery): List<UsedeskArticleBody> {
        return executeRequest(ArticlesBodyPage::class.java,
                apiRetrofit.getArticlesBody(accountId,
                        token,
                        searchQuery.searchQuery,
                        searchQuery.count,
                        searchQuery.getCollectionIds(),
                        searchQuery.getCategoryIds(),
                        searchQuery.getArticleIds(),
                        searchQuery.page,
                        searchQuery.type,
                        searchQuery.sort,
                        searchQuery.order))
                .articles?.toList()
                ?: listOf()
    }

    @Throws(UsedeskHttpException::class)
    override fun addViews(accountId: String, token: String, articleId: Long, count: Int): Int {
        return executeRequest(ViewsAdded::class.java,
                apiRetrofit.addViews(accountId, articleId, token, count))
                .views
    }

    @Throws(UsedeskHttpException::class)
    private fun <T> executeRequest(tClass: Class<T>, call: Call<String>): T {
        try {
            val sectionsResponse = call.execute()
            if (sectionsResponse.isSuccessful && sectionsResponse.body() != null) {
                return try {
                    gson.fromJson(sectionsResponse.body(), tClass)
                } catch (e: JsonSyntaxException) {
                    val (code, error) = gson.fromJson(sectionsResponse.body(), ApiError::class.java)
                    val usedeskHttpException: UsedeskHttpException
                    usedeskHttpException = when (code) {
                        SERVER_ERROR -> UsedeskHttpException(UsedeskHttpException.Error.SERVER_ERROR, error)
                        INVALID_TOKEN -> UsedeskHttpException(UsedeskHttpException.Error.INVALID_TOKEN, error)
                        ACCESS_ERROR -> UsedeskHttpException(UsedeskHttpException.Error.ACCESS_ERROR, error)
                        else -> UsedeskHttpException(error)
                    }
                    throw usedeskHttpException
                } catch (e: IllegalStateException) {
                    val (code, error) = gson.fromJson(sectionsResponse.body(), ApiError::class.java)
                    val usedeskHttpException: UsedeskHttpException
                    usedeskHttpException = when (code) {
                        SERVER_ERROR -> UsedeskHttpException(UsedeskHttpException.Error.SERVER_ERROR, error)
                        INVALID_TOKEN -> UsedeskHttpException(UsedeskHttpException.Error.INVALID_TOKEN, error)
                        ACCESS_ERROR -> UsedeskHttpException(UsedeskHttpException.Error.ACCESS_ERROR, error)
                        else -> UsedeskHttpException(error)
                    }
                    throw usedeskHttpException
                }
            }
        } catch (e: IOException) {
            throw UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.message)
        } catch (e: IllegalStateException) {
            throw UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.message)
        } catch (e: JsonSyntaxException) {
            throw UsedeskHttpException(UsedeskHttpException.Error.JSON_ERROR, e.message)
        }
        throw UsedeskHttpException("Unhandled response")
    }

    companion object {
        private const val SERVER_ERROR = "111"
        private const val INVALID_TOKEN = "112"
        private const val ACCESS_ERROR = "115"
    }
}