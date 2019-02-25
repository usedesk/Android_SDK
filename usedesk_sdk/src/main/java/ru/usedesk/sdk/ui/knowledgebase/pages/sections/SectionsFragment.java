package ru.usedesk.sdk.ui.knowledgebase.pages.sections;


import android.support.v7.widget.RecyclerView;

import java.util.List;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;
import ru.usedesk.sdk.ui.knowledgebase.pages.FragmentListView;

public class SectionsFragment extends FragmentListView<Section, SectionsViewModel> {

    private final KnowledgeBase knowledgeBase;

    public SectionsFragment() {
        knowledgeBase = KnowledgeBase.getInstance();
    }

    public static SectionsFragment newInstance() {
        return new SectionsFragment();
    }

    @Override
    protected ViewModelFactory<SectionsViewModel> getViewModelFactory() {
        return new SectionsViewModel.Factory(knowledgeBase);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<Section> list) {
        if (!(getParentFragment() instanceof IOnSectionClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnSectionClickListener.class.getSimpleName());
        }

        return new SectionsAdapter(list,
                (IOnSectionClickListener) getParentFragment());
    }
}
