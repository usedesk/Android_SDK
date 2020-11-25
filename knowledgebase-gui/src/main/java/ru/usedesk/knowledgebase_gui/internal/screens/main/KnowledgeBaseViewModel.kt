package ru.usedesk.knowledgebase_gui.internal.screens.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
}
