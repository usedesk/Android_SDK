package ru.usedesk.sdk.ui.knowledgebase.main;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import ru.usedesk.sdk.appsdk.KnowledgeBase;

public class KnowledgeBaseViewModel extends ViewModel {

    private KnowledgeBaseViewModel(@NonNull Context context) {
        KnowledgeBase.init(context);
    }

    static ViewModelProvider.Factory getFactory(@NonNull final Context context) {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new KnowledgeBaseViewModel(context);
            }
        };
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        KnowledgeBase.destroy();
    }
}
