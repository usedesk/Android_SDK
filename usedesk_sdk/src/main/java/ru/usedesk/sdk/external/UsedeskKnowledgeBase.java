package ru.usedesk.sdk.external;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.external.entity.knowledgebase.Category;
import ru.usedesk.sdk.external.entity.knowledgebase.KnowledgeBaseConfiguration;
import ru.usedesk.sdk.external.entity.knowledgebase.SearchQuery;
import ru.usedesk.sdk.external.entity.knowledgebase.Section;
import ru.usedesk.sdk.internal.domain.interactor.knowledgebase.IKnowledgeBaseInteractor;

public final class UsedeskKnowledgeBase {

    private IKnowledgeBaseInteractor knowledgeBaseInteractor;

    @Inject
    UsedeskKnowledgeBase(@NonNull IKnowledgeBaseInteractor knowledgeBaseInteractor) {
        this.knowledgeBaseInteractor = knowledgeBaseInteractor;
    }

    public void setConfiguration(@NonNull KnowledgeBaseConfiguration configuration) {
        knowledgeBaseInteractor.setConfiguration(configuration);
    }

    @NonNull
    public Single<List<Section>> getSectionsSingle() {
        return knowledgeBaseInteractor.getSectionsSingle();
    }

    @NonNull
    public Single<ArticleBody> getArticleSingle(long articleId) {
        return knowledgeBaseInteractor.getArticleSingle(articleId);
    }

    @NonNull
    public Single<List<ArticleBody>> getArticlesSingle(@NonNull String searchQuery) {
        return knowledgeBaseInteractor.getArticlesSingle(searchQuery);
    }

    @NonNull
    public Single<List<ArticleBody>> getArticlesSingle(@NonNull SearchQuery searchQuery) {
        return knowledgeBaseInteractor.getArticlesSingle(searchQuery);
    }

    @NonNull
    public Single<List<Category>> getCategoriesSingle(long sectionId) {
        return knowledgeBaseInteractor.getCategoriesSingle(sectionId);
    }

    @NonNull
    public Single<List<ArticleInfo>> getArticlesSingle(long categoryId) {
        return knowledgeBaseInteractor.getArticlesSingle(categoryId);
    }

    @NonNull
    public Completable addViews(long articleId) {
        return knowledgeBaseInteractor.addViewsCompletable(articleId);
    }
}
