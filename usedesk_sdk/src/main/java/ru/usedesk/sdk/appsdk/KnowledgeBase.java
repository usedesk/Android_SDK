package ru.usedesk.sdk.appsdk;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import ru.usedesk.sdk.appsdk.di.KnowledgeBaseScope;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;
import ru.usedesk.sdk.domain.entity.knowledgebase.KnowledgeBaseConfiguration;
import ru.usedesk.sdk.domain.entity.knowledgebase.SearchQuery;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;
import ru.usedesk.sdk.domain.interactor.knowledgebase.IKnowledgeBaseInteractor;
import toothpick.Scope;
import toothpick.Toothpick;

public final class KnowledgeBase {

    private static KnowledgeBase instance;

    private final KnowledgeBaseScope scope;

    @Inject
    IKnowledgeBaseInteractor knowledgeBaseInteractor;

    private KnowledgeBase(@NonNull Context appContext) {
        scope = new KnowledgeBaseScope(this, appContext);
        Toothpick.inject(this, getScope());
    }

    @NonNull
    public static KnowledgeBase init(@NonNull Context context) {
        return instance = new KnowledgeBase(context.getApplicationContext());
    }

    public static void destroy() {
        instance = null;
    }

    @NonNull
    public static KnowledgeBase getInstance() {
        if (instance == null) {
            throw new RuntimeException("You must call " + KnowledgeBase.class.getSimpleName() + ".initViewModel() static method before");
        }
        return instance;
    }

    @NonNull
    private Scope getScope() {
        return scope.getScope();
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
}
