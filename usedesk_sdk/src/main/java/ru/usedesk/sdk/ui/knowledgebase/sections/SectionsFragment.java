package ru.usedesk.sdk.ui.knowledgebase.sections;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public class SectionsFragment extends Fragment {

    private RecyclerView recyclerViewSections;
    private TextView textViewLoading;

    public SectionsFragment() {
    }

    public static SectionsFragment getInstance() {
        return new SectionsFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sections, container, false);

        textViewLoading = view.findViewById(R.id.tv_loading);
        recyclerViewSections = view.findViewById(R.id.rv_list);

        SectionsViewModel viewModel = ViewModelProviders.of(this)
                .get(SectionsViewModel.class);

        viewModel.getSectionsLiveData()
                .observe(this, this::onSectionsLoaded);

        return view;
    }

    private void onSectionsLoaded(List<Section> sections) {
        recyclerViewSections.setAdapter(new SectionListAdapter(sections));
        textViewLoading.setVisibility(View.GONE);
        recyclerViewSections.setVisibility(View.VISIBLE);
    }
}
