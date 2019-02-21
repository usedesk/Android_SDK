package ru.usedesk.sdk.ui.knowledgebase.pages.articlebody;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.FragmentView;

public class ArticlesBodyFragment extends FragmentView<ArticlesBodyViewModel> {

    public static final String SEARCH_QUERY_KEY = "searchQueryKey";

    private final KnowledgeBase knowledgeBase;
    private RecyclerView recyclerViewSections;
    private TextView textViewLoading;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        textViewLoading = view.findViewById(R.id.tv_loading);
        recyclerViewSections = view.findViewById(R.id.rv_list);

        String searchQuery = getNonNullArguments().getString(SEARCH_QUERY_KEY);

        initViewModel(new ArticlesBodyViewModel.Factory(knowledgeBase, searchQuery));

        getViewModel().getArticlesLiveData()
                .observe(this, this::onSectionsLoaded);

        return view;
    }

    private void onSectionsLoaded(List<ArticleBody> articleInfos) {
        if (!(getParentFragment() instanceof IOnArticleBodyClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnArticleBodyClickListener.class.getSimpleName());
        }
        ArticlesBodyAdapter adapter = new ArticlesBodyAdapter(articleInfos,
                (IOnArticleBodyClickListener) getParentFragment());

        recyclerViewSections.setAdapter(adapter);
        recyclerViewSections.setLayoutManager(new LinearLayoutManager(getContext()));

        textViewLoading.setVisibility(View.GONE);
    }
}
