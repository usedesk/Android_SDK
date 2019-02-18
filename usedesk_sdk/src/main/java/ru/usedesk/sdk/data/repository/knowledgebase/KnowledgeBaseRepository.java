package ru.usedesk.sdk.data.repository.knowledgebase;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ru.usedesk.sdk.domain.boundaries.IKnowledgeBaseRepository;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public class KnowledgeBaseRepository implements IKnowledgeBaseRepository {

    private IApiLoader apiLoader;

    @Inject
    KnowledgeBaseRepository(IApiLoader apiLoader) {
        this.apiLoader = apiLoader;
    }

    @NonNull
    @Override
    public List<Section> getSections(@NonNull String id, @NonNull String token) throws ApiException {
        return Arrays.asList(apiLoader.getSections(id, token));
    }

    @NonNull
    @Override
    public ArticleBody getArticle(@NonNull String id, @NonNull String token,
                                  @NonNull ArticleInfo articleInfo) throws ApiException {
        return apiLoader.getArticle(id, articleInfo.getId(), token);
    }
}
