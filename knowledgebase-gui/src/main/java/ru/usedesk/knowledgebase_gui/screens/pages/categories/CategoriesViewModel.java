package ru.usedesk.knowledgebase_gui.screens.pages.categories;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.knowledgebase_gui.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.Category;

class CategoriesViewModel extends DataViewModel<List<Category>> {

    private CategoriesViewModel(@NonNull IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk, long sectionId) {
        loadData(usedeskKnowledgeBaseSdk.getCategoriesSingle(sectionId));
    }

    static class Factory extends ViewModelFactory<CategoriesViewModel> {

        private IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk;
        private long sectionId;

        public Factory(@NonNull IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk, long sectionId) {
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
