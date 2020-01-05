package ru.usedesk.knowledgebase_gui.screens.pages.articlesinfo;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.knowledgebase_gui.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.sdk.external.UsedeskKnowledgeBase;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleInfo;

class ArticlesInfoViewModel extends DataViewModel<List<ArticleInfo>> {

    private ArticlesInfoViewModel(@NonNull UsedeskKnowledgeBase usedeskKnowledgeBase, long categoryId) {
        loadData(usedeskKnowledgeBase.getArticlesSingle(categoryId));
    }

    static class Factory extends ViewModelFactory<ArticlesInfoViewModel> {
        private final UsedeskKnowledgeBase usedeskKnowledgeBase;
        private final long categoryId;

        public Factory(@NonNull UsedeskKnowledgeBase usedeskKnowledgeBase, long categoryId) {
            this.usedeskKnowledgeBase = usedeskKnowledgeBase;
            this.categoryId = categoryId;
        }

        @NonNull
        @Override
        protected ArticlesInfoViewModel create() {
            return new ArticlesInfoViewModel(usedeskKnowledgeBase, categoryId);
        }

        @NonNull
        @Override
        protected Class<ArticlesInfoViewModel> getClassType() {
            return ArticlesInfoViewModel.class;
        }
    }
}
