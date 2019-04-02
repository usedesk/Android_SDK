package ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.ui.knowledgebase.common.DataViewModel;
import ru.usedesk.sdk.ui.knowledgebase.common.ViewModelFactory;

class ArticlesInfoViewModel extends DataViewModel<List<ArticleInfo>> {

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
