package ru.usedesk.knowledgebase_sdk.internal.data.repository;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskDataNotFoundException;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleInfo;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSearchQuery;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.IApiLoader;

public class ApiRepository implements IKnowledgeBaseRepository {

    private IApiLoader apiLoader;

    private List<UsedeskSection> sectionList;

    @Inject
    ApiRepository(IApiLoader apiLoader) {
        this.apiLoader = apiLoader;
    }

    @NonNull
    @Override
    public List<UsedeskSection> getSections(@NonNull String id, @NonNull String token) throws UsedeskHttpException {
        if (sectionList == null) {
            sectionList = Arrays.asList(apiLoader.getSections(id, token));
        }
        return sectionList;
    }

    @NonNull
    @Override
    public UsedeskArticleBody getArticleBody(@NonNull String id, @NonNull String token, long articleId) throws UsedeskHttpException {
        return apiLoader.getArticle(id, Long.toString(articleId), token);
    }

    @NonNull
    @Override
    public List<UsedeskArticleBody> getArticles(@NonNull String accountId, @NonNull String token,
                                                @NonNull UsedeskSearchQuery searchQuery) throws UsedeskHttpException {
        return apiLoader.getArticles(accountId, token, searchQuery);
    }

    @NonNull
    @Override
    public List<UsedeskCategory> getCategories(@NonNull String id, @NonNull String token, long sectionId)
            throws UsedeskHttpException, UsedeskDataNotFoundException {
        if (sectionList == null) {
            getSections(id, token);
        }
        return Arrays.asList(getCategories(sectionId));
    }

    @NonNull
    @Override
    public List<UsedeskArticleInfo> getArticles(@NonNull String id, @NonNull String token, long categoryId)
            throws UsedeskHttpException, UsedeskDataNotFoundException {
        if (sectionList == null) {
            getSections(id, token);
        }
        return Arrays.asList(getArticles(categoryId));
    }

    @Override
    public void addViews(@NonNull String accountId, @NonNull String token, long articleId)
            throws UsedeskHttpException, UsedeskDataNotFoundException {
        int views = apiLoader.addViews(accountId, token, articleId, 1);

        getArticleBody(accountId, token, articleId).setViews(views);
        getArticleInfo(articleId).setViews(views);
    }

    private UsedeskArticleInfo getArticleInfo(long articleId) throws UsedeskDataNotFoundException {
        for (UsedeskSection section : sectionList) {
            for (UsedeskCategory category : section.getCategories())
                for (UsedeskArticleInfo articleInfo : category.getArticles()) {
                    if (articleInfo.getId() == articleId) {
                        return articleInfo;
                    }
                }
        }
        throw new UsedeskDataNotFoundException();
    }

    @NonNull
    private UsedeskArticleInfo[] getArticles(long categoryId) throws UsedeskDataNotFoundException {
        for (UsedeskSection section : sectionList) {
            for (UsedeskCategory category : section.getCategories())
                if (category.getId() == categoryId) {
                    return category.getArticles();
                }
        }
        throw new UsedeskDataNotFoundException("UsedeskCategory with id(" + categoryId + ")");
    }

    @NonNull
    private UsedeskCategory[] getCategories(long sectionId) throws UsedeskDataNotFoundException {
        for (UsedeskSection section : sectionList) {
            if (section.getId() == sectionId) {
                return section.getCategories();
            }
        }
        throw new UsedeskDataNotFoundException("UsedeskSection with id(" + sectionId + ")");
    }
}
