package ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.SearchQuery;
import ru.usedesk.knowledgebase_sdk.external.entity.Section;

public interface IApiLoader {
    @NonNull
    Section[] getSections(@NonNull String accountId, @NonNull String token) throws UsedeskHttpException;

    @NonNull
    ArticleBody getArticle(@NonNull String accountId, @NonNull String articleId, @NonNull String token)
            throws UsedeskHttpException;

    @NonNull
    List<ArticleBody> getArticles(@NonNull String accountId, @NonNull String token,
                                  @NonNull SearchQuery searchQuery)
            throws UsedeskHttpException;

    int addViews(@NonNull String accountId, @NonNull String token, long articleId, int count)
            throws UsedeskHttpException;
}
