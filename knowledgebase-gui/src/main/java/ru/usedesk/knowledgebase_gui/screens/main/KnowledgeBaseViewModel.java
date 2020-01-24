package ru.usedesk.knowledgebase_gui.screens.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;

public class KnowledgeBaseViewModel extends ViewModel {

    private static final int SEARCH_DELAY = 500;

    private MutableLiveData<String> searchQueryLiveData = new MutableLiveData<>();
    private DelayedQuery delayedQuery;

    private KnowledgeBaseViewModel(@NonNull Context context) {
        UsedeskKnowledgeBaseSdk.init(context);

        delayedQuery = new DelayedQuery(searchQueryLiveData, SEARCH_DELAY);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        delayedQuery.dispose();
        UsedeskKnowledgeBaseSdk.release();
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
