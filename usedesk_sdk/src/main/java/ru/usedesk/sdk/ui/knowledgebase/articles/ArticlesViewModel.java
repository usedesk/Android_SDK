package ru.usedesk.sdk.ui.knowledgebase.articles;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;

public class ArticlesViewModel extends ViewModel {

    private final Disposable sectionsDisposable;
    private MutableLiveData<List<ArticleInfo>> articlesLiveData = new MutableLiveData<>();

    public ArticlesViewModel(long categoryId) {//TODO: make factory
        sectionsDisposable = KnowledgeBase.getInstance()
                .getArticlesSingle(categoryId)
                .subscribe(articlesLiveData::setValue);
    }

    LiveData<List<ArticleInfo>> getArticlesLiveData() {
        return articlesLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        sectionsDisposable.dispose();
    }
}
