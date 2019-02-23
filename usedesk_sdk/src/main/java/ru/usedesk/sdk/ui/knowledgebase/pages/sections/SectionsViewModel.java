package ru.usedesk.sdk.ui.knowledgebase.pages.sections;

import android.support.annotation.NonNull;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;
import ru.usedesk.sdk.ui.knowledgebase.pages.ListViewModel;

class SectionsViewModel extends ListViewModel<Section> {

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
