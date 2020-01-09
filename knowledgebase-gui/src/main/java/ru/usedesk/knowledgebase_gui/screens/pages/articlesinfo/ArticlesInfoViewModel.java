package ru.usedesk.knowledgebase_gui.screens.pages.articlesinfo;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.knowledgebase_gui.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleInfo;

class ArticlesInfoViewModel extends DataViewModel<List<ArticleInfo>> {

    private ArticlesInfoViewModel(@NonNull IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk, long categoryId) {
        loadData(usedeskKnowledgeBaseSdk.getArticlesSingle(categoryId));
    }

    static class Factory extends ViewModelFactory<ArticlesInfoViewModel> {
        private final IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk;
        private final long categoryId;

        public Factory(@NonNull IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk, long categoryId) {
            this.usedeskKnowledgeBaseSdk = usedeskKnowledgeBaseSdk;
            this.categoryId = categoryId;
        }

        @NonNull
        @Override
        protected ArticlesInfoViewModel create() {
            return new ArticlesInfoViewModel(usedeskKnowledgeBaseSdk, categoryId);
        }

        @NonNull
        @Override
        protected Class<ArticlesInfoViewModel> getClassType() {
            return ArticlesInfoViewModel.class;
        }
    }
}
