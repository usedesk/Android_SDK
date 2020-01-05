package ru.usedesk.knowledgebase_gui.screens.pages.categories;

import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.knowledgebase_gui.screens.common.DataViewModel;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.sdk.external.UsedeskKnowledgeBase;
import ru.usedesk.sdk.external.entity.knowledgebase.Category;

class CategoriesViewModel extends DataViewModel<List<Category>> {


    private CategoriesViewModel(@NonNull UsedeskKnowledgeBase usedeskKnowledgeBase, long sectionId) {
        loadData(usedeskKnowledgeBase.getCategoriesSingle(sectionId));
    }

    static class Factory extends ViewModelFactory<CategoriesViewModel> {

        private UsedeskKnowledgeBase usedeskKnowledgeBase;
        private long sectionId;

        public Factory(@NonNull UsedeskKnowledgeBase usedeskKnowledgeBase, long sectionId) {
            this.usedeskKnowledgeBase = usedeskKnowledgeBase;
            this.sectionId = sectionId;
        }

        @NonNull
        @Override
        protected CategoriesViewModel create() {
            return new CategoriesViewModel(usedeskKnowledgeBase, sectionId);
        }

        @NonNull
        @Override
        protected Class<CategoriesViewModel> getClassType() {
            return CategoriesViewModel.class;
        }
    }
}
