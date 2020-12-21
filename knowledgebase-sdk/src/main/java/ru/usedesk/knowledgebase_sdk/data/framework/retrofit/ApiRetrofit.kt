package ru.usedesk.knowledgebase_sdk.data.framework.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSearchQueryOld.Order

internal interface ApiRetrofit {
    @GET("{account_id}/list")
    fun getSections(@Path(value = "account_id", encoded = true) accountId: String,
                    @Query("api_token") token: String): Call<String>

    @GET("{account_id}/articles/{article_id}")
    fun getArticleBody(@Path(value = "account_id", encoded = true) accountId: String,
                       @Path(value = "article_id", encoded = true) articleId: String,
                       @Query("api_token") token: String): Call<String>

    @GET("{account_id}/articles/list")
    fun getArticlesBody(@Path(value = "account_id", encoded = true) accountId: String,
                        @Query("api_token") token: String,
                        @Query("query") searchQuery: String,
                        @Query("count") count: String?,
                        @Query("collection_ids") collectionIds: String?,
                        @Query("category_ids") categoryIds: String?,
                        @Query("article_ids") articleIds: String?,
                        @Query("page") page: String?,
                        @Query("type") type: UsedeskSearchQueryOld.Type?,
                        @Query("sort") sort: UsedeskSearchQueryOld.Sort?,
                        @Query("order") order: Order?): Call<String>

    @GET("{account_id}/articles/{article_id}/add-views")
    fun addViews(@Path(value = "account_id", encoded = true) accountId: String,
                 @Path(value = "article_id", encoded = true) articleId: Long,
                 @Query("api_token") token: String,
                 @Query("count") count: Int): Call<String>
}