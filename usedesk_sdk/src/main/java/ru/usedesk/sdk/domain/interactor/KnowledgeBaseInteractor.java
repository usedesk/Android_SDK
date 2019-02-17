package ru.usedesk.sdk.domain.interactor;


import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import ru.usedesk.sdk.domain.boundaries.IKnowledgeRepository;
import ru.usedesk.sdk.domain.boundaries.IUserInfoRepository;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public class KnowledgeBaseInteractor implements IKnowledgeBaseInteractor {

    private IUserInfoRepository userInfoRepository;
    private IKnowledgeRepository knowledgeRepository;
    private Scheduler workScheduler;
    private Scheduler mainThreadScheduler;

    @Inject
    KnowledgeBaseInteractor(IUserInfoRepository userInfoRepository,
                            IKnowledgeRepository knowledgeRepository,
                            Scheduler workScheduler, Scheduler mainThreadScheduler) {
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
    public Single<ArticleBody> getArticleSingle(@NonNull ArticleInfo articleInfo) {
        return Single.create(
                (SingleOnSubscribe<ArticleBody>) emitter -> emitter.onSuccess(getArticle(articleInfo)))
                .subscribeOn(workScheduler)
                .observeOn(mainThreadScheduler);
    }

    @NonNull
    private List<Section> getSections() throws DataNotFoundException, ApiException {
        String id = userInfoRepository.getConfiguration().getCompanyId();
        String token = userInfoRepository.getToken();

        return knowledgeRepository.getSections(id, token);
    }

    @NonNull
    private ArticleBody getArticle(@NonNull ArticleInfo articleInfo) throws DataNotFoundException, ApiException {
        String id = userInfoRepository.getConfiguration().getCompanyId();
        String token = userInfoRepository.getToken();

        return knowledgeRepository.getArticle(id, token, articleInfo);
    }
}
