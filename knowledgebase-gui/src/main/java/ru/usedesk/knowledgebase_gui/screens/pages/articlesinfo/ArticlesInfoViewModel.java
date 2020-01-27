package ru.usedesk.knowledgebase_gui.screens.pages.articlesinfo;

import androidx.annotation.NonNull;

import java.util.List;

import ru.usedesk.knowledgebase_gui.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleInfo;

class ArticlesInfoViewModel extends DataViewModel<List<UsedeskArticleInfo>> {

    private ArticlesInfoViewModel(@NonNull IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk, long categoryId) {
        loadData(usedeskKnowledgeBaseSdk.getArticlesRx(categoryId));
    }

    static class Factory extends ViewModelFactory<ArticlesInfoViewModel> {
        private final IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;
        private final long categoryId;

        public Factory(@NonNull IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk, long categoryId) {
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
