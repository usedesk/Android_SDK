package ru.usedesk.sdk.domain.interactor.knowledgebase;


import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import ru.usedesk.sdk.domain.boundaries.knowledge.IKnowledgeBaseInfoRepository;
import ru.usedesk.sdk.domain.boundaries.knowledge.IKnowledgeBaseRepository;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;
import ru.usedesk.sdk.domain.entity.knowledgebase.KnowledgeBaseConfiguration;
import ru.usedesk.sdk.domain.entity.knowledgebase.SearchQuery;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public class KnowledgeBaseInteractor implements IKnowledgeBaseInteractor {

    private IKnowledgeBaseInfoRepository knowledgeBaseInfoRepository;
    private IKnowledgeBaseRepository knowledgeRepository;
    private Scheduler workScheduler;
    private Scheduler mainThreadScheduler;

    @Inject
    KnowledgeBaseInteractor(IKnowledgeBaseInfoRepository knowledgeBaseInfoRepository,
                            IKnowledgeBaseRepository knowledgeRepository,
                            @Named("work") Scheduler workScheduler,
                            @Named("main") Scheduler mainThreadScheduler) {
        this.knowledgeBaseInfoRepository = knowledgeBaseInfoRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.workScheduler = workScheduler;
        this.mainThreadScheduler = mainThreadScheduler;
    }

    @Override
    public void setConfiguration(@NonNull KnowledgeBaseConfiguration configuration) {
        knowledgeBaseInfoRepository.setConfiguration(configuration);
    }

    @Override
    @NonNull
    public Single<List<Section>> getSectionsSingle() {
        return Single.create(
                (SingleOnSubscribe<List<Section>>) emitter -> emitter.onSuccess(getSections()))
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler);
    }

    @Override
    @NonNull
    public Single<ArticleBody> getArticleSingle(long articleId) {
        return Single.create(
                (SingleOnSubscribe<ArticleBody>) emitter -> emitter.onSuccess(getArticle(articleId)))
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler);
    }

    @NonNull
    @Override
    public Single<List<Category>> getCategoriesSingle(long sectionId) {
        return Single.create(
                (SingleOnSubscribe<List<Category>>) emitter -> emitter.onSuccess(getCategories(sectionId)))
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler);
    }

    @NonNull
    @Override
    public Single<List<ArticleInfo>> getArticlesSingle(long categoryId) {
        return Single.create(
                (SingleOnSubscribe<List<ArticleInfo>>) emitter -> emitter.onSuccess(getArticles(categoryId)))
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler);
    }

    @Override
    @NonNull
    public Single<List<ArticleBody>> getArticlesSingle(@NonNull String searchQuery) {
        return Single.create(
                (SingleOnSubscribe<List<ArticleBody>>) emitter -> emitter.onSuccess(getArticles(searchQuery)))
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler);
    }

    @Override
    @NonNull
    public Single<List<ArticleBody>> getArticlesSingle(@NonNull SearchQuery searchQuery) {
        return Single.create(
                (SingleOnSubscribe<List<ArticleBody>>) emitter -> emitter.onSuccess(getArticles(searchQuery)))
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler);
    }

    @NonNull
    @Override
    public Completable addViewsCompletable(long articleId) {
        return Completable.create(emitter -> {
            addViews(articleId);
            emitter.onComplete();
        })
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler);
    }

    private void addViews(long articleId) throws DataNotFoundException, ApiException {
        KnowledgeBaseConfiguration configuration = knowledgeBaseInfoRepository.getConfiguration();
        knowledgeRepository.addViews(configuration.getAccountId(), configuration.getToken(), articleId);
    }

    @NonNull
    private List<Category> getCategories(long sectionId) throws DataNotFoundException, ApiException {
        KnowledgeBaseConfiguration configuration = knowledgeBaseInfoRepository.getConfiguration();
        return knowledgeRepository.getCategories(configuration.getAccountId(), configuration.getToken(), sectionId);
    }

    @NonNull
    private List<Section> getSections() throws DataNotFoundException, ApiException {
        KnowledgeBaseConfiguration configuration = knowledgeBaseInfoRepository.getConfiguration();
        return knowledgeRepository.getSections(configuration.getAccountId(), configuration.getToken());
    }

    @NonNull
    private ArticleBody getArticle(long articleId) throws DataNotFoundException,
            ApiException {
        KnowledgeBaseConfiguration configuration = knowledgeBaseInfoRepository.getConfiguration();
        return knowledgeRepository.getArticleBody(configuration.getAccountId(), configuration.getToken(), articleId);
    }

    @NonNull
    private List<ArticleBody> getArticles(@NonNull String searchQuery) throws DataNotFoundException,
            ApiException {
        SearchQuery query = new SearchQuery.Builder(searchQuery).build();

        KnowledgeBaseConfiguration configuration = knowledgeBaseInfoRepository.getConfiguration();
        return knowledgeRepository.getArticles(configuration.getAccountId(), configuration.getToken(), query);
    }

    @NonNull
    private List<ArticleBody> getArticles(@NonNull SearchQuery searchQuery) throws DataNotFoundException,
            ApiException {
        KnowledgeBaseConfiguration configuration = knowledgeBaseInfoRepository.getConfiguration();
        return knowledgeRepository.getArticles(configuration.getAccountId(), configuration.getToken(), searchQuery);
    }

    @NonNull
    private List<ArticleInfo> getArticles(long categoryId) throws DataNotFoundException, ApiException {
        KnowledgeBaseConfiguration configuration = knowledgeBaseInfoRepository.getConfiguration();
        return knowledgeRepository.getArticles(configuration.getAccountId(), configuration.getToken(), categoryId);
    }

}
