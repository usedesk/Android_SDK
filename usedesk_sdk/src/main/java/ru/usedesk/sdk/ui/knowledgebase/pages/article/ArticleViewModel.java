package ru.usedesk.sdk.ui.knowledgebase.pages.article;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;

public class ArticleViewModel extends ViewModel {

    private final Disposable disposable;

    private final MutableLiveData<ArticleBody> articleLiveData = new MutableLiveData<>();

    private ArticleViewModel(@NonNull KnowledgeBase knowledgeBase, long articleId) {
        disposable = knowledgeBase.getArticleSingle(articleId)
                .subscribe(articleLiveData::setValue);
    }

    LiveData<ArticleBody> getArticleLiveData() {
        return articleLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }

    static class Factory extends ViewModelFactory<ArticleViewModel> {
        private final KnowledgeBase knowledgeBase;
        private final long articleId;

        public Factory(@NonNull KnowledgeBase knowledgeBase, long articleId) {
            this.knowledgeBase = knowledgeBase;
            this.articleId = articleId;
        }

        @NonNull
        @Override
        protected ArticleViewModel create() {
            return new ArticleViewModel(knowledgeBase, articleId);
        }

        @NonNull
        @Override
        protected Class<ArticleViewModel> getClassType() {
            return ArticleViewModel.class;
        }
    }
}
