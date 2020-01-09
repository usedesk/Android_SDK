package ru.usedesk.knowledgebase_gui.screens.pages.categories;


import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import ru.usedesk.common_gui.external.UsedeskViewCustomizer;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_gui.screens.pages.FragmentListView;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.Category;

public class CategoriesFragment extends FragmentListView<Category, CategoriesViewModel> {

    public static final String SECTION_ID_KEY = "sectionIdKey";
    private final IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk;

    public CategoriesFragment() {
        usedeskKnowledgeBaseSdk = UsedeskKnowledgeBaseSdk.getInstance();
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

        return new CategoriesViewModel.Factory(usedeskKnowledgeBaseSdk, categoryId);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<Category> list) {
        if (!(getParentFragment() instanceof IOnCategoryClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnCategoryClickListener.class.getSimpleName());
        }

        return new CategoriesAdapter(list, (IOnCategoryClickListener) getParentFragment(),
                UsedeskViewCustomizer.getInstance());
    }
}
