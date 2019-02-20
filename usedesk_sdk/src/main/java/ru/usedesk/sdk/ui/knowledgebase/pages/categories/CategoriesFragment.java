package ru.usedesk.sdk.ui.knowledgebase.pages.categories;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.Category;
import ru.usedesk.sdk.ui.knowledgebase.FragmentView;

public class CategoriesFragment extends FragmentView<CategoriesViewModel> {

    public static final String SECTION_ID_KEY = "sectionIdKey";
    private final KnowledgeBase knowledgeBase;
    private RecyclerView recyclerViewSections;
    private TextView textViewLoading;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sections, container, false);

        textViewLoading = view.findViewById(R.id.tv_loading);
        recyclerViewSections = view.findViewById(R.id.rv_list);

        long categoryId = getNonNullArguments().getLong(SECTION_ID_KEY);

        initViewModel(new CategoriesViewModel.Factory(knowledgeBase, categoryId));

        getViewModel().getCategoriesLiveData()
                .observe(this, this::onSectionsLoaded);

        return view;
    }

    private void onSectionsLoaded(List<Category> categories) {
        if (!(getParentFragment() instanceof IOnCategoryClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnCategoryClickListener.class.getSimpleName());
        }
        CategoriesAdapter adapter = new CategoriesAdapter(categories,
                (IOnCategoryClickListener) getParentFragment());

        recyclerViewSections.setAdapter(adapter);
        recyclerViewSections.setLayoutManager(new LinearLayoutManager(getContext()));

        textViewLoading.setVisibility(View.GONE);
    }
}
