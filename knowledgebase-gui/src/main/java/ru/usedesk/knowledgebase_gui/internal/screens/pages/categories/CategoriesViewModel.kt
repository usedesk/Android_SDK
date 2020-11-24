package ru.usedesk.knowledgebase_gui.internal.screens.pages.categories;

import androidx.annotation.NonNull;

import java.util.List;

import ru.usedesk.knowledgebase_gui.internal.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory;

class CategoriesViewModel extends DataViewModel<List<UsedeskCategory>> {

    private CategoriesViewModel(@NonNull IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk, long sectionId) {
        loadData(usedeskKnowledgeBaseSdk.getCategoriesRx(sectionId));
    }

    static class Factory extends ViewModelFactory<CategoriesViewModel> {

        private IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;
        private long sectionId;

        public Factory(@NonNull IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk, long sectionId) {
            this.usedeskKnowledgeBaseSdk = usedeskKnowledgeBaseSdk;
            this.sectionId = sectionId;
        }

        @NonNull
        @Override
        protected CategoriesViewModel create() {
            return new CategoriesViewModel(usedeskKnowledgeBaseSdk, sectionId);
        }

        @NonNull
        @Override
        protected Class<CategoriesViewModel> getClassType() {
            return CategoriesViewModel.class;
        }
    }
}
