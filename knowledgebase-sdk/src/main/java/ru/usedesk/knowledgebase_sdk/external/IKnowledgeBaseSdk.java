package ru.usedesk.knowledgebase_sdk.external;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleInfo;
import ru.usedesk.knowledgebase_sdk.external.entity.Category;
import ru.usedesk.knowledgebase_sdk.external.entity.SearchQuery;
import ru.usedesk.knowledgebase_sdk.external.entity.Section;

public interface IKnowledgeBaseSdk {

    @NonNull
    Single<List<Section>> getSectionsSingle();

    @NonNull
    Single<ArticleBody> getArticleSingle(long articleId);

    @NonNull
    Single<List<ArticleBody>> getArticlesSingle(@NonNull String searchQuery);

    @NonNull
    Single<List<ArticleBody>> getArticlesSingle(@NonNull SearchQuery searchQuery);

    @NonNull
    Single<List<Category>> getCategoriesSingle(long sectionId);

    @NonNull
    Single<List<ArticleInfo>> getArticlesSingle(long categoryId);

    @NonNull
    Completable addViewsCompletable(long articleId);
}
