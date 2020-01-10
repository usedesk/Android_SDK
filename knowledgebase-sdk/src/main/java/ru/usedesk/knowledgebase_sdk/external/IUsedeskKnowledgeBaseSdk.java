package ru.usedesk.knowledgebase_sdk.external;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleInfo;
import ru.usedesk.knowledgebase_sdk.external.entity.Category;
import ru.usedesk.knowledgebase_sdk.external.entity.SearchQuery;
import ru.usedesk.knowledgebase_sdk.external.entity.Section;

public interface IUsedeskKnowledgeBaseSdk {

    @NonNull
    List<Section> getSections() throws UsedeskException;

    @NonNull
    ArticleBody getArticle(long articleId) throws UsedeskException;

    @NonNull
    List<ArticleBody> getArticles(String searchQuery) throws UsedeskException;

    @NonNull
    List<ArticleBody> getArticles(SearchQuery searchQuery) throws UsedeskException;

    @NonNull
    List<Category> getCategories(long sectionId) throws UsedeskException;

    @NonNull
    List<ArticleInfo> getArticles(long categoryId) throws UsedeskException;

    void addViews(long articleId) throws UsedeskException;

    @NonNull
    Single<List<Section>> getSectionsRx();

    @NonNull
    Single<ArticleBody> getArticleRx(long articleId);

    @NonNull
    Single<List<ArticleBody>> getArticlesRx(String searchQuery);

    @NonNull
    Single<List<ArticleBody>> getArticlesRx(SearchQuery searchQuery);

    @NonNull
    Single<List<Category>> getCategoriesRx(long sectionId);

    @NonNull
    Single<List<ArticleInfo>> getArticlesRx(long categoryId);

    @NonNull
    Completable addViewsRx(long articleId);
}
