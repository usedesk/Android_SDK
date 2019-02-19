package ru.usedesk.sdk.ui.knowledgebase.article;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;

public class ArticleViewModel extends ViewModel {

    private final Disposable disposable;

    private final MutableLiveData<ArticleBody> articleLiveData = new MutableLiveData<>();

    public ArticleViewModel(long articleId) {
        disposable = KnowledgeBase.getInstance()
                .getArticleSingle(articleId)
                .subscribe(articleLiveData::setValue);
    }

    public LiveData<ArticleBody> getArticleLiveData() {
        return articleLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }
}
