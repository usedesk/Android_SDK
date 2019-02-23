package ru.usedesk.sdk.ui.knowledgebase.pages.articlebody;


import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.List;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.pages.FragmentListView;
import ru.usedesk.sdk.ui.knowledgebase.pages.ListViewModel;

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
    protected ListViewModel<ArticleBody> initViewModel() {
        String searchQuery = getNonNullArguments().getString(SEARCH_QUERY_KEY);

        initViewModel(new ArticlesBodyViewModel.Factory(knowledgeBase, searchQuery));

        return getViewModel();
    }

    @Override
    protected void onData(List<ArticleBody> articleInfos) {
        if (!(getParentFragment() instanceof IOnArticleBodyClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnArticleBodyClickListener.class.getSimpleName());
        }
        initRecyclerView(new ArticlesBodyAdapter(articleInfos,
                (IOnArticleBodyClickListener) getParentFragment()));
    }
}
