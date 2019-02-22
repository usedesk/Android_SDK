package ru.usedesk.sdk.ui.knowledgebase.pages.articlebody;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;
import ru.usedesk.sdk.utils.LogUtils;

public class ArticlesBodyViewModel extends ViewModel {

    private static final String TAG = ArticlesBodyViewModel.class.getSimpleName();

    private final Disposable disposable;
    private final MutableLiveData<List<ArticleBody>> articlesLiveData = new MutableLiveData<>();

    ArticlesBodyViewModel(@NonNull KnowledgeBase knowledgeBase, @NonNull String searchQuery) {
        disposable = knowledgeBase.getArticlesSingle(searchQuery)
                .subscribe(articlesLiveData::setValue,
                        throwable -> {
                            LogUtils.LOGE(TAG, throwable);
                        });
    }

    LiveData<List<ArticleBody>> getArticlesLiveData() {
        return articlesLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }

    static class Factory extends ViewModelFactory<ArticlesBodyViewModel> {
        private final KnowledgeBase knowledgeBase;
        private final String searchQuery;

        public Factory(@NonNull KnowledgeBase knowledgeBase, String searchQuery) {
            this.knowledgeBase = knowledgeBase;
            this.searchQuery = searchQuery;
        }

        @NonNull
        @Override
        protected ArticlesBodyViewModel create() {
            return new ArticlesBodyViewModel(knowledgeBase, searchQuery);
        }

        @NonNull
        @Override
        protected Class<ArticlesBodyViewModel> getClassType() {
            return ArticlesBodyViewModel.class;
        }
    }
}
