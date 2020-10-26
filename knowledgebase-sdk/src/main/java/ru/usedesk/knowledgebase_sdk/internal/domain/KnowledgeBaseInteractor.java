package ru.usedesk.knowledgebase_sdk.internal.domain;


import androidx.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskException;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleInfo;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskKnowledgeBaseConfiguration;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSearchQuery;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection;
import ru.usedesk.knowledgebase_sdk.internal.data.repository.IKnowledgeBaseRepository;

public class KnowledgeBaseInteractor implements IUsedeskKnowledgeBase {

    private final IKnowledgeBaseRepository knowledgeRepository;
    private final Scheduler workScheduler;
    private final Scheduler mainThreadScheduler;
    private final UsedeskKnowledgeBaseConfiguration configuration;

    @Inject
    KnowledgeBaseInteractor(@NonNull IKnowledgeBaseRepository knowledgeRepository,
                            @NonNull @Named("work") Scheduler workScheduler,
                            @NonNull @Named("main") Scheduler mainThreadScheduler,
                            @NonNull UsedeskKnowledgeBaseConfiguration configuration) {
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
    public Single<List<UsedeskSection>> getSectionsRx() {
        return createSingle(emitter -> emitter.onSuccess(getSections()));
    }

    @Override
    @NonNull
    public Single<UsedeskArticleBody> getArticleRx(long articleId) {
        return createSingle(emitter -> emitter.onSuccess(getArticle(articleId)));
    }

    @NonNull
    @Override
    public Single<List<UsedeskCategory>> getCategoriesRx(long sectionId) {
        return createSingle(emitter -> emitter.onSuccess(getCategories(sectionId)));
    }

    @NonNull
    @Override
    public Single<List<UsedeskArticleInfo>> getArticlesRx(long categoryId) {
        return createSingle(emitter -> emitter.onSuccess(getArticles(categoryId)));
    }

    @Override
    @NonNull
    public Single<List<UsedeskArticleBody>> getArticlesRx(@NonNull String searchQuery) {
        return createSingle(emitter -> emitter.onSuccess(getArticles(searchQuery)));
    }

    @Override
    @NonNull
    public Single<List<UsedeskArticleBody>> getArticlesRx(@NonNull UsedeskSearchQuery searchQuery) {
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
    public List<UsedeskCategory> getCategories(long sectionId) throws UsedeskException {
        return knowledgeRepository.getCategories(configuration.getAccountId(), configuration.getToken(), sectionId);
    }

    @Override
    @NonNull
    public List<UsedeskSection> getSections() throws UsedeskException {
        return knowledgeRepository.getSections(configuration.getAccountId(), configuration.getToken());
    }

    @Override
    @NonNull
    public UsedeskArticleBody getArticle(long articleId) throws UsedeskException {
        return knowledgeRepository.getArticleBody(configuration.getAccountId(), configuration.getToken(), articleId);
    }

    @Override
    @NonNull
    public List<UsedeskArticleBody> getArticles(@NonNull String searchQuery) throws UsedeskException {
        UsedeskSearchQuery query = new UsedeskSearchQuery.Builder(searchQuery).build();

        return knowledgeRepository.getArticles(configuration.getAccountId(), configuration.getToken(), query);
    }

    @Override
    @NonNull
    public List<UsedeskArticleBody> getArticles(@NonNull UsedeskSearchQuery searchQuery) throws UsedeskException {
        return knowledgeRepository.getArticles(configuration.getAccountId(), configuration.getToken(), searchQuery);
    }

    @Override
    @NonNull
    public List<UsedeskArticleInfo> getArticles(long categoryId) throws UsedeskException {
        return knowledgeRepository.getArticles(configuration.getAccountId(), configuration.getToken(), categoryId);
    }

    static class SafeSingleEmitter<T> implements SingleOnSubscribe<T> {
        private final SingleOnSubscribe<T> singleOnSubscribeSafe;

        SafeSingleEmitter(SingleOnSubscribe<T> singleOnSubscribeSafe) {
            this.singleOnSubscribeSafe = singleOnSubscribeSafe;
        }

        @Override
        public void subscribe(@NonNull SingleEmitter<T> emitter) throws Exception {
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
