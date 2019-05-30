package ru.usedesk.sdk.internal.data.repository.knowledgebase;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ru.usedesk.sdk.external.entity.exceptions.ApiException;
import ru.usedesk.sdk.external.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.external.entity.knowledgebase.Category;
import ru.usedesk.sdk.external.entity.knowledgebase.SearchQuery;
import ru.usedesk.sdk.external.entity.knowledgebase.Section;
import ru.usedesk.sdk.internal.domain.repositories.knowledge.IKnowledgeBaseRepository;

public class KnowledgeBaseRepository implements IKnowledgeBaseRepository {

    private IApiLoader apiLoader;

    private List<Section> sectionList;

    @Inject
    KnowledgeBaseRepository(IApiLoader apiLoader) {
        this.apiLoader = apiLoader;
    }

    @NonNull
    @Override
    public List<Section> getSections(@NonNull String id, @NonNull String token) throws ApiException {
        if (sectionList == null) {
            sectionList = Arrays.asList(apiLoader.getSections(id, token));
        }
        return sectionList;
    }

    @NonNull
    @Override
    public ArticleBody getArticleBody(@NonNull String id, @NonNull String token, long articleId) throws ApiException {
        return apiLoader.getArticle(id, Long.toString(articleId), token);
    }

    @NonNull
    @Override
    public List<ArticleBody> getArticles(@NonNull String accountId, @NonNull String token,
                                         @NonNull SearchQuery searchQuery) throws ApiException {
        return apiLoader.getArticles(accountId, token, searchQuery);
    }

    @NonNull
    @Override
    public List<Category> getCategories(@NonNull String id, @NonNull String token, long sectionId)
            throws ApiException, DataNotFoundException {
        if (sectionList == null) {
            getSections(id, token);
        }
        return Arrays.asList(getCategories(sectionId));
    }

    @NonNull
    @Override
    public List<ArticleInfo> getArticles(@NonNull String id, @NonNull String token, long categoryId)
            throws ApiException, DataNotFoundException {
        if (sectionList == null) {
            getSections(id, token);
        }
        return Arrays.asList(getArticles(categoryId));
    }

    @Override
    public void addViews(@NonNull String accountId, @NonNull String token, long articleId)
            throws ApiException, DataNotFoundException {
        int views = apiLoader.addViews(accountId, token, articleId, 1);

        getArticleBody(accountId, token, articleId).setViews(views);
        getArticleInfo(articleId).setViews(views);
    }

    private ArticleInfo getArticleInfo(long articleId) throws DataNotFoundException {
        for (Section section : sectionList) {
            for (Category category : section.getCategories())
                for (ArticleInfo articleInfo : category.getArticles()) {
                    if (articleInfo.getId() == articleId) {
                        return articleInfo;
                    }
                }
        }
        throw new DataNotFoundException();
    }

    @NonNull
    private ArticleInfo[] getArticles(long categoryId) throws DataNotFoundException {
        for (Section section : sectionList) {
            for (Category category : section.getCategories())
                if (category.getId() == categoryId) {
                    return category.getArticles();
                }
        }
        throw new DataNotFoundException("Can't found category with id " + categoryId);
    }

    @NonNull
    private Category[] getCategories(long sectionId) throws DataNotFoundException {
        for (Section section : sectionList) {
            if (section.getId() == sectionId) {
                return section.getCategories();
            }
        }
        throw new DataNotFoundException("Can't found section with id " + sectionId);
    }
}
