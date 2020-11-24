package ru.usedesk.knowledgebase_gui.internal.screens.pages.sections;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.usedesk.common_gui.external.UsedeskViewCustomizer;
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.FragmentListView;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection;

public class SectionsFragment extends FragmentListView<UsedeskSection, SectionsViewModel> {

    private final IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;

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
    protected RecyclerView.Adapter getAdapter(List<UsedeskSection> list) {
        if (!(getParentFragment() instanceof IOnSectionClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnSectionClickListener.class.getSimpleName());
        }

        return new SectionsAdapter(list, (IOnSectionClickListener) getParentFragment(),
                UsedeskViewCustomizer.getInstance());
    }
}
