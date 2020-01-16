package ru.usedesk.knowledgebase_gui.screens.pages.article;

import androidx.annotation.NonNull;

import ru.usedesk.knowledgebase_gui.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleBody;

public class ArticleViewModel extends DataViewModel<ArticleBody> {

    private final IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk;

    private ArticleViewModel(@NonNull IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk, long articleId) {
        this.usedeskKnowledgeBaseSdk = usedeskKnowledgeBaseSdk;

        loadData(usedeskKnowledgeBaseSdk.getArticleRx(articleId));
    }

    @Override
    protected void onData(ArticleBody data) {
        super.onData(data);

        usedeskKnowledgeBaseSdk.addViewsRx(data.getId())
                .subscribe();//TODO: вернуть
    }

    static class Factory extends ViewModelFactory<ArticleViewModel> {
        private final IUsedeskKnowledgeBaseSdk iUsedeskKnowledgeBaseSdk;
        private final long articleId;

        public Factory(@NonNull IUsedeskKnowledgeBaseSdk iUsedeskKnowledgeBaseSdk, long articleId) {
            this.iUsedeskKnowledgeBaseSdk = iUsedeskKnowledgeBaseSdk;
            this.articleId = articleId;
        }

        @NonNull
        @Override
        protected ArticleViewModel create() {
            return new ArticleViewModel(iUsedeskKnowledgeBaseSdk, articleId);
        }

        @NonNull
        @Override
        protected Class<ArticleViewModel> getClassType() {
            return ArticleViewModel.class;
        }
    }
}
