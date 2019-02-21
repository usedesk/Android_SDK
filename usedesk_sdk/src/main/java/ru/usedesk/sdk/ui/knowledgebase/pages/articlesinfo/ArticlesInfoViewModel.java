package ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;

public class ArticlesInfoViewModel extends ViewModel {

    private final Disposable disposable;
    private final MutableLiveData<List<ArticleInfo>> articlesLiveData = new MutableLiveData<>();

    ArticlesInfoViewModel(@NonNull KnowledgeBase knowledgeBase, long categoryId) {
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
