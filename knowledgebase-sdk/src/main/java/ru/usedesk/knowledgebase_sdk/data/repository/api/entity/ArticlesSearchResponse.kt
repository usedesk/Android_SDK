package ru.usedesk.knowledgebase_sdk.data.repository.api.entity

import com.google.gson.annotations.SerializedName

internal class ArticlesSearchResponse {
    var page: Long? = null
    var count: Long? = null
    var articles: Array<ArticleBodyResponse?>? = null

    @SerializedName("last-page")
    var lastPage: Long? = null

    @SerializedName("total-count")
    var totalCount: Long? = null
}