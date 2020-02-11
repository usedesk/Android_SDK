package ru.usedesk.knowledgebase_gui.internal.screens.pages.categories;


import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.usedesk.common_gui.external.UsedeskViewCustomizer;
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.FragmentListView;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory;

public class CategoriesFragment extends FragmentListView<UsedeskCategory, CategoriesViewModel> {

    public static final String SECTION_ID_KEY = "sectionIdKey";
    private final IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;

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
    protected RecyclerView.Adapter getAdapter(List<UsedeskCategory> list) {
        if (!(getParentFragment() instanceof IOnCategoryClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnCategoryClickListener.class.getSimpleName());
        }

        return new CategoriesAdapter(list, (IOnCategoryClickListener) getParentFragment(),
                UsedeskViewCustomizer.getInstance());
    }
}
