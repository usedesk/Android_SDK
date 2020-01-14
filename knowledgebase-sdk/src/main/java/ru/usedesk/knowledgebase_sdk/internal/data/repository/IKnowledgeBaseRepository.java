package ru.usedesk.knowledgebase_sdk.internal.data.repository;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleInfo;
import ru.usedesk.knowledgebase_sdk.external.entity.Category;
import ru.usedesk.knowledgebase_sdk.external.entity.SearchQuery;
import ru.usedesk.knowledgebase_sdk.external.entity.Section;

public interface IKnowledgeBaseRepository {
    @NonNull
    List<Section> getSections(@NonNull String id, @NonNull String token) throws UsedeskHttpException;

    @NonNull
    ArticleBody getArticleBody(@NonNull String id, @NonNull String token, long articleId) throws UsedeskHttpException;

    @NonNull
    List<ArticleBody> getArticles(@NonNull String id, @NonNull String token, @NonNull SearchQuery searchQuery) throws UsedeskHttpException;

    @NonNull
    List<Category> getCategories(@NonNull String id, @NonNull String token, long sectionId) throws UsedeskHttpException, UsedeskDataNotFoundException;

    @NonNull
    List<ArticleInfo> getArticles(@NonNull String id, @NonNull String token, long categoryId) throws UsedeskHttpException, UsedeskDataNotFoundException;

    void addViews(@NonNull String accountId, @NonNull String token, long articleId) throws UsedeskHttpException, UsedeskDataNotFoundException;
}
