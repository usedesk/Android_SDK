package ru.usedesk.sdk.data.repository.knowledgebase;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ru.usedesk.sdk.domain.boundaries.knowledge.IKnowledgeBaseRepository;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.exceptions.DataNotFoundException;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;
import ru.usedesk.sdk.domain.entity.knowledgebase.SearchQuery;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

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
    public ArticleBody getArticle(@NonNull String id, @NonNull String token, long articleId) throws ApiException {
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
