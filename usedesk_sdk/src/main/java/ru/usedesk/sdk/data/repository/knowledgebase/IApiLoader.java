package ru.usedesk.sdk.data.repository.knowledgebase;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public interface IApiLoader {
    @NonNull
    Section[] getSections(@NonNull String accountId, @NonNull String token) throws ApiException;

    @NonNull
    ArticleBody getArticle(@NonNull String accountId, @NonNull String articleId, @NonNull String token)
            throws ApiException;
}
