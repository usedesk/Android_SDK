package ru.usedesk.knowledgebase_sdk.external;

import androidx.annotation.NonNull;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleInfo;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSearchQuery;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection;

public interface IUsedeskKnowledgeBase {

    @NonNull
    List<UsedeskSection> getSections() throws UsedeskException;

    @NonNull
    UsedeskArticleBody getArticle(long articleId) throws UsedeskException;

    @NonNull
    List<UsedeskArticleBody> getArticles(String searchQuery) throws UsedeskException;

    @NonNull
    List<UsedeskArticleBody> getArticles(UsedeskSearchQuery searchQuery) throws UsedeskException;

    @NonNull
    List<UsedeskCategory> getCategories(long sectionId) throws UsedeskException;

    @NonNull
    List<UsedeskArticleInfo> getArticles(long categoryId) throws UsedeskException;

    void addViews(long articleId) throws UsedeskException;

    @NonNull
    Single<List<UsedeskSection>> getSectionsRx();

    @NonNull
    Single<UsedeskArticleBody> getArticleRx(long articleId);

    @NonNull
    Single<List<UsedeskArticleBody>> getArticlesRx(String searchQuery);

    @NonNull
    Single<List<UsedeskArticleBody>> getArticlesRx(UsedeskSearchQuery searchQuery);

    @NonNull
    Single<List<UsedeskCategory>> getCategoriesRx(long sectionId);

    @NonNull
    Single<List<UsedeskArticleInfo>> getArticlesRx(long categoryId);

    @NonNull
    Completable addViewsRx(long articleId);
}
