package ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;
import ru.usedesk.sdk.ui.knowledgebase.pages.ListViewModel;

class ArticlesInfoViewModel extends ListViewModel<ArticleInfo> {

    private ArticlesInfoViewModel(@NonNull KnowledgeBase knowledgeBase, long categoryId) {
        loadData(knowledgeBase.getArticlesSingle(categoryId));
    }

    static class Factory extends ViewModelFactory<ArticlesInfoViewModel> {
        private final KnowledgeBase knowledgeBase;
        private final long categoryId;

        public Factory(@NonNull KnowledgeBase knowledgeBase, long categoryId) {
            this.knowledgeBase = knowledgeBase;
            this.categoryId = categoryId;
        }

        @NonNull
        @Override
        protected ArticlesInfoViewModel create() {
            return new ArticlesInfoViewModel(knowledgeBase, categoryId);
        }

        @NonNull
        @Override
        protected Class<ArticlesInfoViewModel> getClassType() {
            return ArticlesInfoViewModel.class;
        }
    }
}
