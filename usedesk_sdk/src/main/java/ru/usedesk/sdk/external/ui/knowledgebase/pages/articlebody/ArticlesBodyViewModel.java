package ru.usedesk.sdk.external.ui.knowledgebase.pages.articlebody;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.external.UsedeskKnowledgeBase;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.external.ui.knowledgebase.common.DataViewModel;
import ru.usedesk.sdk.external.ui.knowledgebase.common.ViewModelFactory;

class ArticlesBodyViewModel extends DataViewModel<List<ArticleBody>> {

    private UsedeskKnowledgeBase usedeskKnowledgeBase;

    private ArticlesBodyViewModel(@NonNull UsedeskKnowledgeBase usedeskKnowledgeBase, @NonNull String searchQuery) {
        this.usedeskKnowledgeBase = usedeskKnowledgeBase;
        onSearchQueryUpdate(searchQuery);
    }

    public void onSearchQueryUpdate(@NonNull String searchQuery) {
        loadData(usedeskKnowledgeBase.getArticlesSingle(searchQuery));
    }

    static class Factory extends ViewModelFactory<ArticlesBodyViewModel> {
        private final UsedeskKnowledgeBase usedeskKnowledgeBase;
        private final String searchQuery;

        public Factory(@NonNull UsedeskKnowledgeBase usedeskKnowledgeBase, String searchQuery) {
            this.usedeskKnowledgeBase = usedeskKnowledgeBase;
            this.searchQuery = searchQuery;
        }

        @NonNull
        @Override
        protected ArticlesBodyViewModel create() {
            return new ArticlesBodyViewModel(usedeskKnowledgeBase, searchQuery);
        }

        @NonNull
        @Override
        protected Class<ArticlesBodyViewModel> getClassType() {
            return ArticlesBodyViewModel.class;
        }
    }
}
