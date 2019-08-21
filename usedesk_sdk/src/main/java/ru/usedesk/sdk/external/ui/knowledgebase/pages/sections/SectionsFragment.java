package ru.usedesk.sdk.external.ui.knowledgebase.pages.sections;


import android.support.v7.widget.RecyclerView;

import java.util.List;

import ru.usedesk.sdk.external.UsedeskKnowledgeBase;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.knowledgebase.Section;
import ru.usedesk.sdk.external.ui.knowledgebase.common.ViewModelFactory;
import ru.usedesk.sdk.external.ui.knowledgebase.pages.FragmentListView;

public class SectionsFragment extends FragmentListView<Section, SectionsViewModel> {

    private final UsedeskKnowledgeBase usedeskKnowledgeBase;

    public SectionsFragment() {
        usedeskKnowledgeBase = UsedeskSdk.getUsedeskKnowledgeBase();
    }

    public static SectionsFragment newInstance() {
        return new SectionsFragment();
    }

    @Override
    protected ViewModelFactory<SectionsViewModel> getViewModelFactory() {
        return new SectionsViewModel.Factory(usedeskKnowledgeBase);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<Section> list) {
        if (!(getParentFragment() instanceof IOnSectionClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnSectionClickListener.class.getSimpleName());
        }

        return new SectionsAdapter(list, (IOnSectionClickListener) getParentFragment(),
                UsedeskSdk.getUsedeskViewCustomizer());
    }
}
