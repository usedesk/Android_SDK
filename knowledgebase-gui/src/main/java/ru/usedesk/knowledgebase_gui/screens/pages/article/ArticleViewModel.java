package ru.usedesk.knowledgebase_gui.screens.pages.article;

import androidx.annotation.NonNull;

import ru.usedesk.knowledgebase_gui.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleBody;

public class ArticleViewModel extends DataViewModel<ArticleBody> {

    private final IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;

    private ArticleViewModel(@NonNull IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk, long articleId) {
        this.usedeskKnowledgeBaseSdk = usedeskKnowledgeBaseSdk;

        loadData(usedeskKnowledgeBaseSdk.getArticleRx(articleId));
    }

    @Override
    protected void onData(ArticleBody data) {
        super.onData(data);

        usedeskKnowledgeBaseSdk.addViewsRx(data.getId())
                .subscribe(() -> {
                }, Throwable::printStackTrace);
    }

    static class Factory extends ViewModelFactory<ArticleViewModel> {
        private final IUsedeskKnowledgeBase iUsedeskKnowledgeBase;
        private final long articleId;

        public Factory(@NonNull IUsedeskKnowledgeBase iUsedeskKnowledgeBase, long articleId) {
            this.iUsedeskKnowledgeBase = iUsedeskKnowledgeBase;
            this.articleId = articleId;
        }

        @NonNull
        @Override
        protected ArticleViewModel create() {
            return new ArticleViewModel(iUsedeskKnowledgeBase, articleId);
        }

        @NonNull
        @Override
        protected Class<ArticleViewModel> getClassType() {
            return ArticleViewModel.class;
        }
    }
}
