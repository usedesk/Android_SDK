package ru.usedesk.sdk.ui.knowledgebase.article;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.usedesk.sdk.R;

public class ArticleFragment extends Fragment {

    private static final String ARTICLE_ID_KEY = "articleIdKey";

    public ArticleFragment() {
    }

    public static ArticleFragment newInstance(long articleId) {
        ArticleFragment articleFragment = new ArticleFragment();

        Bundle args = new Bundle();
        args.putLong(ARTICLE_ID_KEY, articleId);

        articleFragment.setArguments(args);
        return articleFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, container, false);

        if (getArguments() == null) {
            throw new RuntimeException("You must use newInstance method for creating fragment with arguments");
        }

        long articleId = getArguments().getLong(ARTICLE_ID_KEY);

        ArticleViewModel viewModel = ViewModelProviders.of(this)
                .get(ArticleViewModel.class);//TODO: put key

        return view;
    }

}
