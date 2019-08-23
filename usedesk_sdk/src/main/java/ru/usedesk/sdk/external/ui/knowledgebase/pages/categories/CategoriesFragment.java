package ru.usedesk.sdk.external.ui.knowledgebase.pages.categories;


import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import ru.usedesk.sdk.external.UsedeskKnowledgeBase;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.knowledgebase.Category;
import ru.usedesk.sdk.external.ui.knowledgebase.common.ViewModelFactory;
import ru.usedesk.sdk.external.ui.knowledgebase.pages.FragmentListView;

public class CategoriesFragment extends FragmentListView<Category, CategoriesViewModel> {

    public static final String SECTION_ID_KEY = "sectionIdKey";
    private final UsedeskKnowledgeBase usedeskKnowledgeBase;

    public CategoriesFragment() {
        usedeskKnowledgeBase = UsedeskSdk.getUsedeskKnowledgeBase();
    }

    public static CategoriesFragment newInstance(long sectionId) {
        Bundle args = new Bundle();
        args.putLong(SECTION_ID_KEY, sectionId);

        CategoriesFragment fragment = new CategoriesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected ViewModelFactory<CategoriesViewModel> getViewModelFactory() {
        long categoryId = getNonNullArguments().getLong(SECTION_ID_KEY);

        return new CategoriesViewModel.Factory(usedeskKnowledgeBase, categoryId);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<Category> list) {
        if (!(getParentFragment() instanceof IOnCategoryClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnCategoryClickListener.class.getSimpleName());
        }

        return new CategoriesAdapter(list, (IOnCategoryClickListener) getParentFragment(),
                UsedeskSdk.getUsedeskViewCustomizer());
    }
}
