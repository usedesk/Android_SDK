package ru.usedesk.sdk.external.ui.knowledgebase.pages.article;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.external.UsedeskKnowledgeBase;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.external.ui.knowledgebase.common.DataViewModel;
import ru.usedesk.sdk.external.ui.knowledgebase.common.ViewModelFactory;

public class ArticleViewModel extends DataViewModel<ArticleBody> {

    private final UsedeskKnowledgeBase usedeskKnowledgeBase;

    private ArticleViewModel(@NonNull UsedeskKnowledgeBase usedeskKnowledgeBase, long articleId) {
        this.usedeskKnowledgeBase = usedeskKnowledgeBase;
        loadData(this.usedeskKnowledgeBase.getArticleSingle(articleId));
    }

    @Override
    protected void onData(ArticleBody data) {
        super.onData(data);

        usedeskKnowledgeBase.addViews(data.getId()).subscribe();
    }

    static class Factory extends ViewModelFactory<ArticleViewModel> {
        private final UsedeskKnowledgeBase usedeskKnowledgeBase;
        private final long articleId;

        public Factory(@NonNull UsedeskKnowledgeBase usedeskKnowledgeBase, long articleId) {
            this.usedeskKnowledgeBase = usedeskKnowledgeBase;
            this.articleId = articleId;
        }

        @NonNull
        @Override
        protected ArticleViewModel create() {
            return new ArticleViewModel(usedeskKnowledgeBase, articleId);
        }

        @NonNull
        @Override
        protected Class<ArticleViewModel> getClassType() {
            return ArticleViewModel.class;
        }
    }
}
