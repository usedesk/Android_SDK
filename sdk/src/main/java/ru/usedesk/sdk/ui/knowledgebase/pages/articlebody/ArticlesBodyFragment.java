package ru.usedesk.sdk.ui.knowledgebase.pages.articlebody;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.common.ViewModelFactory;
import ru.usedesk.sdk.ui.knowledgebase.pages.FragmentListView;

public class ArticlesBodyFragment extends FragmentListView<ArticleBody, ArticlesBodyViewModel> {

    public static final String SEARCH_QUERY_KEY = "searchQueryKey";

    private final KnowledgeBase knowledgeBase;

    public ArticlesBodyFragment() {
        knowledgeBase = KnowledgeBase.getInstance();
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

        return new ArticlesBodyViewModel.Factory(knowledgeBase, searchQuery);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<ArticleBody> list) {
        if (!(getParentFragment() instanceof IOnArticleBodyClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnArticleBodyClickListener.class.getSimpleName());
        }
        return new ArticlesBodyAdapter(list, (IOnArticleBodyClickListener) getParentFragment(),
                knowledgeBase.getViewCustomizer());
    }

    public void onSearchQueryUpdate(@NonNull String searchQuery) {
        getViewModel().onSearchQueryUpdate(searchQuery);
    }
}
