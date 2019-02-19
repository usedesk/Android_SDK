package ru.usedesk.sdk.ui.knowledgebase.categories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;

public class CategoriesViewModel extends ViewModel {

    private final Disposable disposable;
    private MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();

    public CategoriesViewModel(long sectionId) {//TODO: make factory
        disposable = KnowledgeBase.getInstance()
                .getCategoriesSingle(sectionId)
                .subscribe(categories -> {
                    categoriesLiveData.setValue(categories);
                });
    }

    LiveData<List<Category>> getCategoriesLiveData() {
        return categoriesLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }
}
