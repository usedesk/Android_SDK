package ru.usedesk.sdk.external.ui.knowledgebase.pages.sections;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.external.UsedeskKnowledgeBase;
import ru.usedesk.sdk.external.entity.knowledgebase.Section;
import ru.usedesk.sdk.external.ui.knowledgebase.common.DataViewModel;
import ru.usedesk.sdk.external.ui.knowledgebase.common.ViewModelFactory;

class SectionsViewModel extends DataViewModel<List<Section>> {

    private SectionsViewModel(@NonNull UsedeskKnowledgeBase usedeskKnowledgeBase) {
        loadData(usedeskKnowledgeBase.getSectionsSingle());
    }

    static class Factory extends ViewModelFactory<SectionsViewModel> {
        private final UsedeskKnowledgeBase usedeskKnowledgeBase;

        public Factory(@NonNull UsedeskKnowledgeBase usedeskKnowledgeBase) {
            this.usedeskKnowledgeBase = usedeskKnowledgeBase;
        }

        @NonNull
        @Override
        protected SectionsViewModel create() {
            return new SectionsViewModel(usedeskKnowledgeBase);
        }

        @NonNull
        @Override
        protected Class<SectionsViewModel> getClassType() {
            return SectionsViewModel.class;
        }
    }
}
