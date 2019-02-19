package ru.usedesk.sdk.domain.interactor.knowledgebase;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Single;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public interface IKnowledgeBaseInteractor {

    @NonNull
    Single<List<Section>> getSectionsSingle();

    @NonNull
    Single<ArticleBody> getArticleSingle(@NonNull ArticleInfo articleInfo);

    @NonNull
    Single<List<ArticleBody>> getArticlesSingle(@NonNull String searchQuery);

    @NonNull
    Single<List<Category>> getCategoriesSingle(long sectionId);

    @NonNull
    Single<List<ArticleInfo>> getArticlesSingle(long categoryId);
}
