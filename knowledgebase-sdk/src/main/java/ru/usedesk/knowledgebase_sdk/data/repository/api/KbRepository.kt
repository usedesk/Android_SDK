package ru.usedesk.knowledgebase_sdk.data.repository.api

import com.google.gson.Gson
import ru.usedesk.common_sdk.api.UsedeskApiFactory
import ru.usedesk.common_sdk.api.UsedeskApiRepository
import ru.usedesk.common_sdk.api.multipart.UsedeskMultipartConverter
import ru.usedesk.knowledgebase_sdk.data.repository.api.UsedeskKnowledgeBase.AddViewsResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.UsedeskKnowledgeBase.GetArticleResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.UsedeskKnowledgeBase.GetArticlesResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.UsedeskKnowledgeBase.GetSectionsResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.UsedeskKnowledgeBase.SendRatingResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.UsedeskKnowledgeBase.SendReviewResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.AddRating
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.AddViews
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.CategoryResponse
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.CreateTicket
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.GetArticleContent
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.GetArticles
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.LoadSections
import ru.usedesk.knowledgebase_sdk.data.repository.api.entity.SectionResponse
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleContent
import ru.usedesk.knowledgebase_sdk.entity.UsedeskArticleInfo
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory
import ru.usedesk.knowledgebase_sdk.entity.UsedeskKnowledgeBaseConfiguration
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection
import javax.inject.Inject

internal class KbRepository @Inject constructor(
    private val configuration: UsedeskKnowledgeBaseConfiguration,
    multipartConverter: UsedeskMultipartConverter,
    apiFactory: UsedeskApiFactory,
    gson: Gson
) : UsedeskApiRepository<ApiRetrofit>(
    apiFactory,
    multipartConverter,
    gson,
    ApiRetrofit::class.java
), UsedeskKnowledgeBase {

    private fun Array<CategoryResponse?>.convert() = mapNotNull { categoryResponse ->
        valueOrNull {
            val categoryId = categoryResponse!!.id!!
            val articles = categoryResponse.articles?.mapNotNull { articleResponse ->
                valueOrNull {
                    UsedeskArticleInfo(
                        id = articleResponse!!.id!!,
                        title = articleResponse.title ?: "",
                        categoryId = categoryId,
                        viewsCount = articleResponse.views ?: 0
                    )
                }
            } ?: listOf()

            UsedeskCategory(
                id = categoryId,
                title = categoryResponse.title ?: "",
                description = categoryResponse.description ?: "",
                articles = articles
            )
        }
    }

    private fun Array<SectionResponse?>.convert() = mapNotNull { sectionResponse ->
        valueOrNull {
            val categories = sectionResponse!!.categories?.convert() ?: listOf()
            UsedeskSection(
                id = sectionResponse.id!!,
                title = sectionResponse.title ?: "",
                thumbnail = sectionResponse.image,
                categories = categories
            )
        }
    }

    override fun getSections(): GetSectionsResponse {
        val request = LoadSections.Request(
            apiToken = configuration.token,
            accountId = configuration.accountId
        )
        val response = doRequestJson(
            urlApi = configuration.urlApi,
            body = request,
            responseClass = LoadSections.Response::class.java
        ) {
            getSections(
                accountId = it.accountId,
                token = it.apiToken
            )
        }
        return when (response?.items) {
            null -> GetSectionsResponse.Error(response?.code)
            else -> GetSectionsResponse.Done(response.items.convert())
        }
    }

    override fun getArticle(articleId: Long): GetArticleResponse {
        val request = GetArticleContent.Request(
            accountId = configuration.accountId,
            articleId = articleId,
            token = configuration.token
        )
        val response = doRequestJson(
            urlApi = configuration.urlApi,
            body = request,
            responseClass = GetArticleContent.Response::class.java
        ) {
            getArticleContent(
                accountId = it.accountId,
                articleId = it.articleId,
                token = it.token
            )
        }
        return when (val articleContent = response?.convert()) {
            null -> GetArticleResponse.Error(response?.code)
            else -> GetArticleResponse.Done(articleContent)
        }
    }

    override fun getArticles(
        query: String,
        page: Long
    ): GetArticlesResponse {
        val request = GetArticles.Request(
            query = query,
            page = page,
            type = GetArticles.Request.Type.PUBLIC
        )
        val response = doRequestJson(
            urlApi = configuration.urlApi,
            body = request,
            responseClass = GetArticles.Response::class.java
        ) { request ->
            getArticles(
                accountId = configuration.accountId,
                token = configuration.token,
                searchQuery = request.query,
                count = request.count,
                collectionIds = request.sectionIds,
                categoryIds = request.categoryIds,
                articleIds = request.articleIds,
                page = request.page,
                type = request.type,
                sort = request.sort,
                order = request.order
            )
        }

        return when (response?.articles) {
            null -> GetArticlesResponse.Error(response?.code)
            else -> GetArticlesResponse.Done(response.articles.mapNotNull { it?.convert() })
        }
    }

    private fun GetArticleContent.Response.convert() = valueOrNull {
        UsedeskArticleContent(
            id = id!!,
            title = title ?: "",
            categoryId = categoryId?.toLongOrNull()!!,
            viewsCount = views ?: 0,
            text = ARTICLE_STYLE + injectIframePermissions(text ?: ""),
            public = public == 1
        )
    }

    /**
     * Ensures every `<iframe>` carries `allowfullscreen` and an `allow` policy that includes
     * `fullscreen`. The attributes must be present on the parsed element — patching them later via
     * DOM API doesn't help RuTube/YouTube show the fullscreen button.
     *
     * Supports both double-quoted (`allow="..."`) and single-quoted (`allow='...'`) attribute
     * forms; un-quoted attribute values are not in the HTML5 iframe spec and are not handled.
     */
    private fun injectIframePermissions(html: String): String =
        IFRAME_OPEN_TAG_REGEX.replace(html) { match ->
            var attrs = match.groupValues[1]
            if (!ALLOWFULLSCREEN_ATTR_REGEX.containsMatchIn(attrs)) {
                attrs = "$attrs allowfullscreen"
            }
            val allow = ALLOW_ATTR_REGEX.find(attrs)
            attrs = when {
                allow == null -> "$attrs allow=\"$IFRAME_ALLOW_DEFAULT\""
                // groupValues[1] is the quote char (" or '), groupValues[2] is the value inside.
                allow.groupValues[2].contains("fullscreen", ignoreCase = true) -> attrs
                else -> {
                    val quote = allow.groupValues[1]
                    val merged = allow.groupValues[2].trimEnd().trimEnd(';').trim()
                    val newValue = if (merged.isEmpty()) "fullscreen" else "$merged; fullscreen"
                    attrs.replaceRange(allow.range, "allow=$quote$newValue$quote")
                }
            }
            "<iframe$attrs>"
        }

    override fun addViews(articleId: Long): AddViewsResponse {
        val request = AddViews.Request(
            configuration.token,
            configuration.accountId,
            articleId
        )
        val response = doRequestJson(
            configuration.urlApi,
            request,
            AddViews.Response::class.java
        ) { request ->
            addViews(
                accountId = request.accountId,
                articleId = request.articleId,
                token = request
            )
        }
        return when (response?.views) {
            null -> AddViewsResponse.Error(response?.code)
            else -> AddViewsResponse.Done(response.views)
        }
    }

    override fun sendRating(
        articleId: Long,
        good: Boolean
    ): SendRatingResponse {
        val request = AddRating.Request(
            configuration.token,
            configuration.accountId,
            articleId,
            if (good) 1 else 0,
            if (good) 0 else 1
        )
        val response = doRequestJson(
            urlApi = configuration.urlApi,
            body = request,
            responseClass = AddRating.Response::class.java
        ) {
            changeRating(
                accountId = request.accountId,
                articleId = request.articleId,
                body = request,
            )
        }
        return when (response?.rating) {
            null -> SendRatingResponse.Error(response?.code)
            else -> SendRatingResponse.Done(
                response.rating.positive ?: 0,
                response.rating.positive ?: 0
            )
        }
    }

    override fun sendReview(
        subject: String,
        message: String
    ): SendReviewResponse {
        val request = CreateTicket.Request(
            apiToken = configuration.token,
            clientEmail = configuration.clientEmail,
            clientName = configuration.clientName,
            subject = subject,
            message = message
        )
        val response = doRequestJson(
            urlApi = configuration.urlApi,
            body = request,
            responseClass = CreateTicket.Response::class.java,
            getCall = ApiRetrofit::createTicket
        )
        return when (response?.status) {
            "success" -> SendReviewResponse.Done()
            else -> SendReviewResponse.Error(response?.code)
        }
    }

    companion object {
        private const val ARTICLE_STYLE =
            "<style>img{display: inline;height: auto;max-width: 100%;}iframe{display: block;max-width: 100%;height: auto;overflow-x: auto;}body{overflow-wrap: break-word;}</style>"

        private const val IFRAME_ALLOW_DEFAULT =
            "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share; fullscreen"

        private val IFRAME_OPEN_TAG_REGEX = Regex("<iframe\\b([^>]*)>", RegexOption.IGNORE_CASE)
        private val ALLOWFULLSCREEN_ATTR_REGEX = Regex("\\ballowfullscreen\\b", RegexOption.IGNORE_CASE)

        // Group 1 captures the opening quote (" or '), group 2 captures the value inside it.
        // The backreference \1 in [^\1] would be nice but Kotlin Regex doesn't support that inside
        // a character class — we constrain the value by matching anything except the same quote
        // char via a tempered alternation.
        private val ALLOW_ATTR_REGEX = Regex(
            "\\ballow\\s*=\\s*(\"|')((?:(?!\\1).)*)\\1",
            RegexOption.IGNORE_CASE
        )
    }
}