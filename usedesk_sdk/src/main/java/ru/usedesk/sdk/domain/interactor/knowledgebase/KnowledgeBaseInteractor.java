package ru.usedesk.sdk.domain.interactor.knowledgebase;


import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import ru.usedesk.sdk.domain.boundaries.IKnowledgeBaseRepository;
import ru.usedesk.sdk.domain.boundaries.IUserInfoRepository;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public class KnowledgeBaseInteractor implements IKnowledgeBaseInteractor {

    private IUserInfoRepository userInfoRepository;
    private IKnowledgeBaseRepository knowledgeRepository;
    private Scheduler workScheduler;
    private Scheduler mainThreadScheduler;

    @Inject
    KnowledgeBaseInteractor(IUserInfoRepository userInfoRepository,
                            IKnowledgeBaseRepository knowledgeRepository,
                            @Named("work") Scheduler workScheduler,
                            @Named("main") Scheduler mainThreadScheduler) {
        this.userInfoRepository = userInfoRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.workScheduler = workScheduler;
        this.mainThreadScheduler = mainThreadScheduler;
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

    @NonNull
    private List<Category> getCategories(long sectionId) throws DataNotFoundException, ApiException {
        String id = userInfoRepository.getConfiguration().getCompanyId();
        String token = userInfoRepository.getToken();

        id = "4";
        token = "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75";

        return knowledgeRepository.getCategories(id, token, sectionId);
    }

    @NonNull
    private List<Section> getSections() throws DataNotFoundException, ApiException {
        String id = userInfoRepository.getConfiguration().getCompanyId();
        String token = userInfoRepository.getToken();

        id = "4";
        token = "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75";

        return knowledgeRepository.getSections(id, token);
    }

    @NonNull
    private ArticleBody getArticle(long articleId) throws DataNotFoundException,
            ApiException {
        String id = userInfoRepository.getConfiguration().getCompanyId();
        String token = userInfoRepository.getToken();

        id = "4";
        token = "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75";

        return knowledgeRepository.getArticle(id, token, articleId);
    }

    @NonNull
    private List<ArticleBody> getArticles(@NonNull String searchQuery) throws DataNotFoundException,
            ApiException {
        String id = userInfoRepository.getConfiguration().getCompanyId();
        String token = userInfoRepository.getToken();

        id = "4";
        token = "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75";

        return knowledgeRepository.getArticles(id, token, searchQuery);
    }

    @NonNull
    private List<ArticleInfo> getArticles(long categoryId) throws DataNotFoundException, ApiException {
        String id = userInfoRepository.getConfiguration().getCompanyId();
        String token = userInfoRepository.getToken();

        id = "4";
        token = "11eb3f39dec94ecf0fe4a80349903e6ad5ce6d75";

        return knowledgeRepository.getArticles(id, token, categoryId);
    }

}
