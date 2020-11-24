package ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.usedesk.common_gui.external.UsedeskViewCustomizer;
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_gui.internal.screens.pages.FragmentListView;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody;

public class ArticlesBodyFragment extends FragmentListView<UsedeskArticleBody, ArticlesBodyViewModel> {

    public static final String SEARCH_QUERY_KEY = "searchQueryKey";

    private final IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;

    public ArticlesBodyFragment() {
        usedeskKnowledgeBaseSdk = UsedeskKnowledgeBaseSdk.getInstance();
    }

    public static ArticlesBodyFragment newInstance(@NonNull String searchQuery) {
        Bundle args = new Bundle();
        args.putString(SEARCH_QUERY_KEY, searchQuery);

        ArticlesBodyFragment articlesBodyFragment = new ArticlesBodyFragment();
        articlesBodyFragment.setArguments(args);
        return articlesBodyFragment;
    }

    @Override
    protected ViewModelFactory<ArticlesBodyViewModel> getViewModelFactory() {
        String searchQuery = getNonNullArguments().getString(SEARCH_QUERY_KEY);

        return new ArticlesBodyViewModel.Factory(usedeskKnowledgeBaseSdk, searchQuery);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<UsedeskArticleBody> list) {
        if (!(getParentFragment() instanceof IOnArticleBodyClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnArticleBodyClickListener.class.getSimpleName());
        }
        return new ArticlesBodyAdapter(list, (IOnArticleBodyClickListener) getParentFragment(),
                UsedeskViewCustomizer.getInstance());
    }

    public void onSearchQueryUpdate(@NonNull String searchQuery) {
        getViewModel().onSearchQueryUpdate(searchQuery);
    }
}
