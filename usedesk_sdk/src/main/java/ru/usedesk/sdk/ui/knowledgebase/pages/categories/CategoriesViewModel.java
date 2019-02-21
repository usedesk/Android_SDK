package ru.usedesk.sdk.ui.knowledgebase.pages.categories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;

public class CategoriesViewModel extends ViewModel {

    private final Disposable disposable;
    private final MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();

    CategoriesViewModel(@NonNull KnowledgeBase knowledgeBase, long sectionId) {
        disposable = knowledgeBase.getCategoriesSingle(sectionId)
                .subscribe(categoriesLiveData::setValue);
    }

    LiveData<List<Category>> getCategoriesLiveData() {
        return categoriesLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }

    static class Factory extends ViewModelFactory<CategoriesViewModel> {

        private KnowledgeBase knowledgeBase;
        private long sectionId;

        public Factory(@NonNull KnowledgeBase knowledgeBase, long sectionId) {
            this.knowledgeBase = knowledgeBase;
            this.sectionId = sectionId;
        }

        @NonNull
        @Override
        protected CategoriesViewModel create() {
            return new CategoriesViewModel(knowledgeBase, sectionId);
        }

        @NonNull
        @Override
        protected Class<CategoriesViewModel> getClassType() {
            return CategoriesViewModel.class;
        }
    }
}
