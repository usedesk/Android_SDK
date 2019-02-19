package ru.usedesk.sdk.ui.knowledgebase.sections;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public class SectionsViewModel extends ViewModel {

    private final Disposable disposable;
    private MutableLiveData<List<Section>> sectionsLiveData = new MutableLiveData<>();

    public SectionsViewModel() {
        disposable = KnowledgeBase.getInstance()
                .getSectionsSingle()
                .subscribe(sections -> {
                    sectionsLiveData.setValue(sections);
                });
    }

    LiveData<List<Section>> getSectionsLiveData() {
        return sectionsLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }
}
