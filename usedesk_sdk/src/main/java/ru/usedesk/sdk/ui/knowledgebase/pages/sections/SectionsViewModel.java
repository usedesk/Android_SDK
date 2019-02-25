package ru.usedesk.sdk.ui.knowledgebase.pages.sections;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;
import ru.usedesk.sdk.ui.knowledgebase.DataViewModel;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;

class SectionsViewModel extends DataViewModel<List<Section>> {

    private SectionsViewModel(@NonNull KnowledgeBase knowledgeBase) {
        loadData(knowledgeBase.getSectionsSingle());
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
