package ru.usedesk.sdk.domain.boundaries;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public interface IKnowledgeRepository {
    @NonNull
    List<Section> getSections(@NonNull String id, @NonNull String token) throws ApiException;

    @NonNull
    ArticleBody getArticle(@NonNull String id, @NonNull String token,
                           @NonNull ArticleInfo articleInfo) throws ApiException;
}
