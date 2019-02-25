package ru.usedesk.sdk.ui.knowledgebase.pages.article;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.DataViewModel;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;

public class ArticleViewModel extends DataViewModel<ArticleBody> {

    private ArticleViewModel(@NonNull KnowledgeBase knowledgeBase, long articleId) {
        loadData(knowledgeBase.getArticleSingle(articleId));
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
