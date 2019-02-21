package ru.usedesk.sdk.ui.knowledgebase.pages.sections;


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
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;
import ru.usedesk.sdk.ui.knowledgebase.FragmentView;

public class SectionsFragment extends FragmentView<SectionsViewModel> {

    private final KnowledgeBase knowledgeBase;
    private RecyclerView recyclerViewSections;
    private TextView textViewLoading;

    public SectionsFragment() {
        knowledgeBase = KnowledgeBase.getInstance();
    }

    public static SectionsFragment newInstance() {
        return new SectionsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        textViewLoading = view.findViewById(R.id.tv_loading);
        recyclerViewSections = view.findViewById(R.id.rv_list);

        initViewModel(new SectionsViewModel.Factory(knowledgeBase));

        getViewModel().getSectionsLiveData()
                .observe(this, this::onSectionsLoaded);

        return view;
    }

    private void onSectionsLoaded(List<Section> sections) {
        if (!(getParentFragment() instanceof IOnSectionClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnSectionClickListener.class.getSimpleName());
        }
        SectionsAdapter adapter = new SectionsAdapter(sections,
                (IOnSectionClickListener) getParentFragment());

        recyclerViewSections.setAdapter(adapter);
        recyclerViewSections.setLayoutManager(new LinearLayoutManager(getContext()));

        textViewLoading.setVisibility(View.GONE);
    }
}
