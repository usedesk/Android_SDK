package ru.usedesk.knowledgebase_sdk.internal.domain;


import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleInfo;
import ru.usedesk.knowledgebase_sdk.external.entity.Category;
import ru.usedesk.knowledgebase_sdk.external.entity.KnowledgeBaseConfiguration;
import ru.usedesk.knowledgebase_sdk.external.entity.SearchQuery;
import ru.usedesk.knowledgebase_sdk.external.entity.Section;
import ru.usedesk.knowledgebase_sdk.internal.data.repository.IKnowledgeBaseRepository;

public class KnowledgeBase implements IUsedeskKnowledgeBaseSdk {

    private final IKnowledgeBaseRepository knowledgeRepository;
    private final Scheduler workScheduler;
    private final Scheduler mainThreadScheduler;
    private final KnowledgeBaseConfiguration configuration;

    @Inject
    KnowledgeBase(@NonNull IKnowledgeBaseRepository knowledgeRepository,
                  @NonNull @Named("work") Scheduler workScheduler,
                  @NonNull @Named("main") Scheduler mainThreadScheduler,
                  @NonNull KnowledgeBaseConfiguration configuration) {
        this.knowledgeRepository = knowledgeRepository;
        this.workScheduler = workScheduler;
        this.mainThreadScheduler = mainThreadScheduler;
        this.configuration = configuration;
    }

    private <T> Single<T> createSingle(SingleOnSubscribe<T> emitter) {
        return Single.create(new SafeSingleEmitter<>(emitter))
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler);
    }

    @Override
    @NonNull
    public Single<List<Section>> getSectionsRx() {
        return createSingle(emitter -> emitter.onSuccess(getSections()));
    }

    @Override
    @NonNull
    public Single<ArticleBody> getArticleRx(long articleId) {
        return createSingle(emitter -> emitter.onSuccess(getArticle(articleId)));
    }

    @NonNull
    @Override
    public Single<List<Category>> getCategoriesRx(long sectionId) {
        return createSingle(emitter -> emitter.onSuccess(getCategories(sectionId)));
    }

    @NonNull
    @Override
    public Single<List<ArticleInfo>> getArticlesRx(long categoryId) {
        return createSingle(emitter -> emitter.onSuccess(getArticles(categoryId)));
    }

    @Override
    @NonNull
    public Single<List<ArticleBody>> getArticlesRx(@NonNull String searchQuery) {
        return createSingle(emitter -> emitter.onSuccess(getArticles(searchQuery)));
    }

    @Override
    @NonNull
    public Single<List<ArticleBody>> getArticlesRx(@NonNull SearchQuery searchQuery) {
        return createSingle(emitter -> emitter.onSuccess(getArticles(searchQuery)));
    }

    @Override
    @NonNull
    public Completable addViewsRx(long articleId) {
        return Completable.create(emitter -> {
            addViews(articleId);
            emitter.onComplete();
        }).subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler);
    }

    @Override
    public void addViews(long articleId) throws UsedeskException {
        knowledgeRepository.addViews(configuration.getAccountId(), configuration.getToken(), articleId);
    }

    @Override
    @NonNull
    public List<Category> getCategories(long sectionId) throws UsedeskException {
        return knowledgeRepository.getCategories(configuration.getAccountId(), configuration.getToken(), sectionId);
    }

    @Override
    @NonNull
    public List<Section> getSections() throws UsedeskException {
        return knowledgeRepository.getSections(configuration.getAccountId(), configuration.getToken());
    }

    @Override
    @NonNull
    public ArticleBody getArticle(long articleId) throws UsedeskException {
        return knowledgeRepository.getArticleBody(configuration.getAccountId(), configuration.getToken(), articleId);
    }

    @Override
    @NonNull
    public List<ArticleBody> getArticles(@NonNull String searchQuery) throws UsedeskException {
        SearchQuery query = new SearchQuery.Builder(searchQuery).build();

        return knowledgeRepository.getArticles(configuration.getAccountId(), configuration.getToken(), query);
    }

    @Override
    @NonNull
    public List<ArticleBody> getArticles(@NonNull SearchQuery searchQuery) throws UsedeskException {
        return knowledgeRepository.getArticles(configuration.getAccountId(), configuration.getToken(), searchQuery);
    }

    @Override
    @NonNull
    public List<ArticleInfo> getArticles(long categoryId) throws UsedeskException {
        return knowledgeRepository.getArticles(configuration.getAccountId(), configuration.getToken(), categoryId);
    }

    class SafeSingleEmitter<T> implements SingleOnSubscribe<T> {
        private final SingleOnSubscribe<T> singleOnSubscribeSafe;

        SafeSingleEmitter(SingleOnSubscribe<T> singleOnSubscribeSafe) {
            this.singleOnSubscribeSafe = singleOnSubscribeSafe;
        }

        @Override
        public void subscribe(SingleEmitter<T> emitter) throws Exception {
            try {
                singleOnSubscribeSafe.subscribe(emitter);
            } catch (Exception e) {
                if (!emitter.isDisposed()) {
                    throw e;
                }
            }
        }
    }
}
