package ru.usedesk.knowledgebase_gui.screens.pages.sections;

import androidx.annotation.NonNull;

import java.util.List;

import ru.usedesk.knowledgebase_gui.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.Section;

class SectionsViewModel extends DataViewModel<List<Section>> {

    private SectionsViewModel(@NonNull IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk) {
        loadData(usedeskKnowledgeBaseSdk.getSectionsRx());
    }

    static class Factory extends ViewModelFactory<SectionsViewModel> {
        private final IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk;

        public Factory(@NonNull IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk) {
            this.usedeskKnowledgeBaseSdk = usedeskKnowledgeBaseSdk;
        }

        @NonNull
        @Override
        protected SectionsViewModel create() {
            return new SectionsViewModel(usedeskKnowledgeBaseSdk);
        }

        @NonNull
        @Override
        protected Class<SectionsViewModel> getClassType() {
            return SectionsViewModel.class;
        }
    }
}
