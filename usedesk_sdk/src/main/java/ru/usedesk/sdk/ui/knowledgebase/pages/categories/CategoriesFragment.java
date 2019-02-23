package ru.usedesk.sdk.ui.knowledgebase.pages.categories;


import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;
import ru.usedesk.sdk.ui.knowledgebase.pages.FragmentListView;

public class CategoriesFragment extends FragmentListView<Category, CategoriesViewModel> {

    public static final String SECTION_ID_KEY = "sectionIdKey";
    private final KnowledgeBase knowledgeBase;

    public CategoriesFragment() {
        knowledgeBase = KnowledgeBase.getInstance();
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

        return new CategoriesViewModel.Factory(knowledgeBase, categoryId);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<Category> list) {
        if (!(getParentFragment() instanceof IOnCategoryClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnCategoryClickListener.class.getSimpleName());
        }

        return new CategoriesAdapter(list,
                (IOnCategoryClickListener) getParentFragment());
    }
}
