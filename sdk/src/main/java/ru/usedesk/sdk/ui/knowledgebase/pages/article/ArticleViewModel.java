package ru.usedesk.sdk.ui.knowledgebase.pages.article;

import android.support.annotation.NonNull;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.DataViewModel;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;

public class ArticleViewModel extends DataViewModel<ArticleBody> {

    private final KnowledgeBase knowledgeBase;

    private ArticleViewModel(@NonNull KnowledgeBase knowledgeBase, long articleId) {
        this.knowledgeBase = knowledgeBase;
        loadData(this.knowledgeBase.getArticleSingle(articleId));
    }

    @Override
    protected void onData(ArticleBody data) {
        super.onData(data);

        Disposable disposable = knowledgeBase.addViews(data.getId())
                .subscribe(() -> {

                }, throwable -> {
                    throwable = throwable;
                });
    }

    static class Factory extends ViewModelFactory<ArticleViewModel> {
        private final KnowledgeBase knowledgeBase;
        private final long articleId;

        public Factory(@NonNull KnowledgeBase knowledgeBase, long articleId) {
            this.knowledgeBase = knowledgeBase;
            this.articleId = articleId;
        }

        @NonNull
        @Override
        protected ArticleViewModel create() {
            return new ArticleViewModel(knowledgeBase, articleId);
        }

        @NonNull
        @Override
        protected Class<ArticleViewModel> getClassType() {
            return ArticleViewModel.class;
        }
    }
}
