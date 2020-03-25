package ru.usedesk.knowledgebase_sdk.internal.data.repository;

import androidx.annotation.NonNull;

import java.util.List;

import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleInfo;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSearchQuery;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection;

public interface IKnowledgeBaseRepository {
    @NonNull
    List<UsedeskSection> getSections(@NonNull String id, @NonNull String token) throws UsedeskHttpException;

    @NonNull
    UsedeskArticleBody getArticleBody(@NonNull String id, @NonNull String token, long articleId) throws UsedeskHttpException;

    @NonNull
    List<UsedeskArticleBody> getArticles(@NonNull String id, @NonNull String token, @NonNull UsedeskSearchQuery searchQuery) throws UsedeskHttpException;

    @NonNull
    List<UsedeskCategory> getCategories(@NonNull String id, @NonNull String token, long sectionId) throws UsedeskHttpException, UsedeskDataNotFoundException;

    @NonNull
    List<UsedeskArticleInfo> getArticles(@NonNull String id, @NonNull String token, long categoryId) throws UsedeskHttpException, UsedeskDataNotFoundException;

    void addViews(@NonNull String accountId, @NonNull String token, long articleId) throws UsedeskHttpException, UsedeskDataNotFoundException;
}
