package ru.usedesk.sdk.ui.knowledgebase.pages.articles;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;

public class ArticlesViewModel extends ViewModel {

    private final Disposable disposable;
    private MutableLiveData<List<ArticleInfo>> articlesLiveData = new MutableLiveData<>();

    public ArticlesViewModel(@NonNull KnowledgeBase knowledgeBase, long categoryId) {
        disposable = knowledgeBase.getArticlesSingle(categoryId)
                .subscribe(articlesLiveData::setValue);
    }

    LiveData<List<ArticleInfo>> getArticlesLiveData() {
        return articlesLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }

    static class Factory extends ViewModelFactory<ArticlesViewModel> {
        private final KnowledgeBase knowledgeBase;
        private final long categoryId;

        public Factory(@NonNull KnowledgeBase knowledgeBase, long categoryId) {
            this.knowledgeBase = knowledgeBase;
            this.categoryId = categoryId;
        }

        @NonNull
        @Override
        protected ArticlesViewModel create() {
            return new ArticlesViewModel(knowledgeBase, categoryId);
        }

        @NonNull
        @Override
        protected Class<ArticlesViewModel> getClassType() {
            return ArticlesViewModel.class;
        }
    }
}
