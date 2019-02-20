package ru.usedesk.sdk.ui.knowledgebase.pages.sections;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;

public class SectionsViewModel extends ViewModel {

    private final Disposable disposable;
    private MutableLiveData<List<Section>> sectionsLiveData = new MutableLiveData<>();

    private SectionsViewModel(@NonNull KnowledgeBase knowledgeBase) {
        disposable = knowledgeBase.getSectionsSingle()
                .subscribe(sectionsLiveData::setValue);
    }

    LiveData<List<Section>> getSectionsLiveData() {
        return sectionsLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        disposable.dispose();
    }

    static class Factory extends ViewModelFactory<SectionsViewModel> {
        private final KnowledgeBase knowledgeBase;

        public Factory(@NonNull KnowledgeBase knowledgeBase) {
            this.knowledgeBase = knowledgeBase;
        }

        @NonNull
        @Override
        protected SectionsViewModel create() {
            return new SectionsViewModel(knowledgeBase);
        }

        @NonNull
        @Override
        protected Class<SectionsViewModel> getClassType() {
            return SectionsViewModel.class;
        }
    }
}
