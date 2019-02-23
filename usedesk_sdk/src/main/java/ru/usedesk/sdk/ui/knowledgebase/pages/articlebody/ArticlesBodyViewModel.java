package ru.usedesk.sdk.ui.knowledgebase.pages.articlebody;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;
import ru.usedesk.sdk.ui.knowledgebase.pages.ListViewModel;

class ArticlesBodyViewModel extends ListViewModel<ArticleBody> {

    private ArticlesBodyViewModel(@NonNull KnowledgeBase knowledgeBase, @NonNull String searchQuery) {
        loadData(knowledgeBase.getArticlesSingle(searchQuery));
    }

    static class Factory extends ViewModelFactory<ArticlesBodyViewModel> {
        private final KnowledgeBase knowledgeBase;
        private final String searchQuery;

        public Factory(@NonNull KnowledgeBase knowledgeBase, String searchQuery) {
            this.knowledgeBase = knowledgeBase;
            this.searchQuery = searchQuery;
        }

        @NonNull
        @Override
        protected ArticlesBodyViewModel create() {
            return new ArticlesBodyViewModel(knowledgeBase, searchQuery);
        }

        @NonNull
        @Override
        protected Class<ArticlesBodyViewModel> getClassType() {
            return ArticlesBodyViewModel.class;
        }
    }
}
