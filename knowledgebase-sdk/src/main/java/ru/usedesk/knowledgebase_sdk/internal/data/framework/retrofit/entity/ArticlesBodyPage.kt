package ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.entity

import com.google.gson.annotations.SerializedName
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody

class ArticlesBodyPage {
    val page = 0

    @SerializedName("last-page")
    val lastPage = 0

    val count = 0

    @SerializedName("total-count")
    val totalCount = 0

    val articles: Array<UsedeskArticleBody>? = null
}