package ru.usedesk.sdk.internal.domain.repositories.knowledge;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.external.entity.exceptions.ApiException;
import ru.usedesk.sdk.external.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.external.entity.knowledgebase.Category;
import ru.usedesk.sdk.external.entity.knowledgebase.SearchQuery;
import ru.usedesk.sdk.external.entity.knowledgebase.Section;

public interface IKnowledgeBaseRepository {
    @NonNull
    List<Section> getSections(@NonNull String id, @NonNull String token) throws ApiException;

    @NonNull
    ArticleBody getArticleBody(@NonNull String id, @NonNull String token, long articleId) throws ApiException;

    @NonNull
    List<ArticleBody> getArticles(@NonNull String id, @NonNull String token,
                                  @NonNull SearchQuery searchQuery) throws ApiException;

    @NonNull
    List<Category> getCategories(@NonNull String id, @NonNull String token, long sectionId)
            throws ApiException, DataNotFoundException;

    @NonNull
    List<ArticleInfo> getArticles(@NonNull String id, @NonNull String token,
                                  long categoryId) throws ApiException, DataNotFoundException;

    void addViews(@NonNull String accountId, @NonNull String token, long articleId)
            throws ApiException, DataNotFoundException;
}
