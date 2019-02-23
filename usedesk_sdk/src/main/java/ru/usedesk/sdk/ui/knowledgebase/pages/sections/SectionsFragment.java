package ru.usedesk.sdk.ui.knowledgebase.pages.sections;


import java.util.List;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;
import ru.usedesk.sdk.ui.knowledgebase.pages.FragmentListView;
import ru.usedesk.sdk.ui.knowledgebase.pages.ListViewModel;

public class SectionsFragment extends FragmentListView<Section, SectionsViewModel> {

    private final KnowledgeBase knowledgeBase;

    public SectionsFragment() {
        knowledgeBase = KnowledgeBase.getInstance();
    }

    public static SectionsFragment newInstance() {
        return new SectionsFragment();
    }

    @Override
    protected ListViewModel<Section> initViewModel() {
        initViewModel(new SectionsViewModel.Factory(knowledgeBase));
        return getViewModel();
    }

    @Override
    protected void onData(List<Section> sections) {
        if (!(getParentFragment() instanceof IOnSectionClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnSectionClickListener.class.getSimpleName());
        }

        initRecyclerView(new SectionsAdapter(sections,
                (IOnSectionClickListener) getParentFragment()));
    }
}
