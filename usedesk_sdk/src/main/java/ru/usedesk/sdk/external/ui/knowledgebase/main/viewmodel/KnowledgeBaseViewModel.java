package ru.usedesk.sdk.external.ui.knowledgebase.main.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;

import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.ui.knowledgebase.common.ViewModelFactory;

public class KnowledgeBaseViewModel extends ViewModel {

    private static final int SEARCH_DELAY = 500;

    private MutableLiveData<String> searchQueryLiveData = new MutableLiveData<>();
    private DelayedQuery delayedQuery;

    private KnowledgeBaseViewModel(@NonNull Context context) {
        UsedeskSdk.initKnowledgeBase(context);

        delayedQuery = new DelayedQuery(searchQueryLiveData, SEARCH_DELAY);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        delayedQuery.dispose();
        UsedeskSdk.releaseUsedeskKnowledgeBase();
    }

    public void onSearchQuery(@NonNull String query) {
        delayedQuery.onNext(query);
    }

    public LiveData<String> getSearchQueryLiveData() {
        return searchQueryLiveData;
    }

    public static class Factory extends ViewModelFactory<KnowledgeBaseViewModel> {

        private final Context context;

        public Factory(@NonNull Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        protected KnowledgeBaseViewModel create() {
            return new KnowledgeBaseViewModel(context);
        }

        @NonNull
        @Override
        protected Class<KnowledgeBaseViewModel> getClassType() {
            return KnowledgeBaseViewModel.class;
        }
    }
}
