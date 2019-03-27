package ru.usedesk.sdk.ui.knowledgebase.pages.articlebody;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.DataViewModel;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;

class ArticlesBodyViewModel extends DataViewModel<List<ArticleBody>> {

    private KnowledgeBase knowledgeBase;

    private ArticlesBodyViewModel(@NonNull KnowledgeBase knowledgeBase, @NonNull String searchQuery) {
        this.knowledgeBase = knowledgeBase;
        onSearchQueryUpdate(searchQuery);
    }

    public void onSearchQueryUpdate(@NonNull String searchQuery) {
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
