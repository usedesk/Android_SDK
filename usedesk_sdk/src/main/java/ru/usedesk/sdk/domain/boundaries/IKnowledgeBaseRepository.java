package ru.usedesk.sdk.domain.boundaries;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;
import ru.usedesk.sdk.domain.entity.knowledgebase.SearchQuery;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public interface IKnowledgeBaseRepository {
    @NonNull
    List<Section> getSections(@NonNull String id, @NonNull String token) throws ApiException;

    @NonNull
    ArticleBody getArticle(@NonNull String id, @NonNull String token, long articleId) throws ApiException;

    @NonNull
    List<ArticleBody> getArticles(@NonNull String id, @NonNull String token,
                                  @NonNull SearchQuery searchQuery) throws ApiException;

    @NonNull
    List<Category> getCategories(@NonNull String id, @NonNull String token, long sectionId)
            throws ApiException, DataNotFoundException;

    @NonNull
    List<ArticleInfo> getArticles(@NonNull String id, @NonNull String token,
                                  long categoryId) throws ApiException, DataNotFoundException;
}
