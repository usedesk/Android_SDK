package ru.usedesk.knowledgebase_gui.screens.pages.articlebody;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.knowledgebase_gui.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleBody;

class ArticlesBodyViewModel extends DataViewModel<List<ArticleBody>> {

    private IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk;

    private ArticlesBodyViewModel(@NonNull IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk, @NonNull String searchQuery) {
        this.usedeskKnowledgeBaseSdk = usedeskKnowledgeBaseSdk;
        onSearchQueryUpdate(searchQuery);
    }

    void onSearchQueryUpdate(@NonNull String searchQuery) {
        loadData(usedeskKnowledgeBaseSdk.getArticlesRx(searchQuery));
    }

    static class Factory extends ViewModelFactory<ArticlesBodyViewModel> {
        private final IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk;
        private final String searchQuery;

        public Factory(@NonNull IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk, String searchQuery) {
            this.usedeskKnowledgeBaseSdk = usedeskKnowledgeBaseSdk;
            this.searchQuery = searchQuery;
        }

        @NonNull
        @Override
        protected ArticlesBodyViewModel create() {
            return new ArticlesBodyViewModel(usedeskKnowledgeBaseSdk, searchQuery);
        }

        @NonNull
        @Override
        protected Class<ArticlesBodyViewModel> getClassType() {
            return ArticlesBodyViewModel.class;
        }
    }
}
