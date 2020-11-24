package ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit;

import androidx.annotation.NonNull;

import java.util.List;

import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSearchQuery;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection;

public interface IApiLoader {
    @NonNull
    UsedeskSection[] getSections(@NonNull String accountId, @NonNull String token) throws UsedeskHttpException;

    @NonNull
    UsedeskArticleBody getArticle(@NonNull String accountId, @NonNull String articleId, @NonNull String token)
            throws UsedeskHttpException;

    @NonNull
    List<UsedeskArticleBody> getArticles(@NonNull String accountId, @NonNull String token,
                                         @NonNull UsedeskSearchQuery searchQuery)
            throws UsedeskHttpException;

    int addViews(@NonNull String accountId, @NonNull String token, long articleId, int count)
            throws UsedeskHttpException;
}
