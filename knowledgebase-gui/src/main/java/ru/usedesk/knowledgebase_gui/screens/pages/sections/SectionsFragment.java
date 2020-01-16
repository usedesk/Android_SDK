package ru.usedesk.knowledgebase_gui.screens.pages.sections;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.usedesk.common_gui.external.UsedeskViewCustomizer;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_gui.screens.pages.FragmentListView;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.Section;

public class SectionsFragment extends FragmentListView<Section, SectionsViewModel> {

    private final IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk;

    public SectionsFragment() {
        usedeskKnowledgeBaseSdk = UsedeskKnowledgeBaseSdk.getInstance();
    }

    public static SectionsFragment newInstance() {
        return new SectionsFragment();
    }

    @Override
    protected ViewModelFactory<SectionsViewModel> getViewModelFactory() {
        return new SectionsViewModel.Factory(usedeskKnowledgeBaseSdk);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<Section> list) {
        if (!(getParentFragment() instanceof IOnSectionClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnSectionClickListener.class.getSimpleName());
        }

        return new SectionsAdapter(list, (IOnSectionClickListener) getParentFragment(),
                UsedeskViewCustomizer.getInstance());
    }
}
