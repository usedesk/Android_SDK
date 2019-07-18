package ru.usedesk.sdk.internal.data.repository.knowledgebase;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.external.entity.exceptions.ApiException;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.external.entity.knowledgebase.SearchQuery;
import ru.usedesk.sdk.external.entity.knowledgebase.Section;

public interface IApiLoader {
    @NonNull
    Section[] getSections(@NonNull String accountId, @NonNull String token) throws ApiException;

    @NonNull
    ArticleBody getArticle(@NonNull String accountId, @NonNull String articleId, @NonNull String token)
            throws ApiException;

    @NonNull
    List<ArticleBody> getArticles(@NonNull String accountId, @NonNull String token,
                                  @NonNull SearchQuery searchQuery)
            throws ApiException;

    int addViews(@NonNull String accountId, @NonNull String token, long articleId, int count)
            throws ApiException;
}
