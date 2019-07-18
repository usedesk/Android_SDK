package ru.usedesk.sdk.external.ui.knowledgebase.pages.articlebody;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import ru.usedesk.sdk.external.UsedeskKnowledgeBase;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.external.ui.knowledgebase.common.ViewModelFactory;
import ru.usedesk.sdk.external.ui.knowledgebase.pages.FragmentListView;

public class ArticlesBodyFragment extends FragmentListView<ArticleBody, ArticlesBodyViewModel> {

    public static final String SEARCH_QUERY_KEY = "searchQueryKey";

    private final UsedeskKnowledgeBase usedeskKnowledgeBase;

    public ArticlesBodyFragment() {
        usedeskKnowledgeBase = UsedeskSdk.getUsedeskKnowledgeBase();
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

        return new ArticlesBodyViewModel.Factory(usedeskKnowledgeBase, searchQuery);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<ArticleBody> list) {
        if (!(getParentFragment() instanceof IOnArticleBodyClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnArticleBodyClickListener.class.getSimpleName());
        }
        return new ArticlesBodyAdapter(list, (IOnArticleBodyClickListener) getParentFragment(),
                usedeskKnowledgeBase.getViewCustomizer());
    }

    public void onSearchQueryUpdate(@NonNull String searchQuery) {
        getViewModel().onSearchQueryUpdate(searchQuery);
    }
}
