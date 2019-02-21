package ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo;


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
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.ui.knowledgebase.FragmentView;

public class ArticlesInfoFragment extends FragmentView<ArticlesInfoViewModel> {

    public static final String CATEGORY_ID_KEY = "categoryIdKey";

    private final KnowledgeBase knowledgeBase;
    private RecyclerView recyclerViewSections;
    private TextView textViewLoading;

    public ArticlesInfoFragment() {
        knowledgeBase = KnowledgeBase.getInstance();
    }

    public static ArticlesInfoFragment newInstance(long categoryId) {
        Bundle args = new Bundle();
        args.putLong(CATEGORY_ID_KEY, categoryId);

        ArticlesInfoFragment articlesInfoFragment = new ArticlesInfoFragment();
        articlesInfoFragment.setArguments(args);
        return articlesInfoFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        textViewLoading = view.findViewById(R.id.tv_loading);
        recyclerViewSections = view.findViewById(R.id.rv_list);

        long categoryId = getNonNullArguments().getLong(CATEGORY_ID_KEY);

        initViewModel(new ArticlesInfoViewModel.Factory(knowledgeBase, categoryId));

        getViewModel().getArticlesLiveData()
                .observe(this, this::onSectionsLoaded);

        return view;
    }

    private void onSectionsLoaded(List<ArticleInfo> articleInfos) {
        if (!(getParentFragment() instanceof IOnArticleInfoClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnArticleInfoClickListener.class.getSimpleName());
        }
        ArticlesInfoAdapter adapter = new ArticlesInfoAdapter(articleInfos,
                (IOnArticleInfoClickListener) getParentFragment());

        recyclerViewSections.setAdapter(adapter);
        recyclerViewSections.setLayoutManager(new LinearLayoutManager(getContext()));

        textViewLoading.setVisibility(View.GONE);
    }
}
