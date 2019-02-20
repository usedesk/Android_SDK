package ru.usedesk.sdk.ui.knowledgebase.pages.article;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.FragmentView;

public class ArticleFragment extends FragmentView<ArticleViewModel> {

    private static final String ARTICLE_ID_KEY = "articleIdKey";

    private final KnowledgeBase knowledgeBase;

    private TextView textViewTitle;
    private TextView textViewText;

    public ArticleFragment() {
        knowledgeBase = KnowledgeBase.getInstance();
    }

    public static ArticleFragment newInstance(long articleId) {
        Bundle args = new Bundle();
        args.putLong(ARTICLE_ID_KEY, articleId);

        ArticleFragment articleFragment = new ArticleFragment();
        articleFragment.setArguments(args);
        return articleFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, container, false);

        textViewTitle = view.findViewById(R.id.tv_title);
        textViewText = view.findViewById(R.id.tv_text);

        long articleId = getNonNullArguments().getLong(ARTICLE_ID_KEY);

        initViewModel(new ArticleViewModel.Factory(knowledgeBase, articleId));

        getViewModel().getArticleLiveData()
                .observe(this, this::onArticleBody);

        return view;
    }

    private void onArticleBody(@NonNull ArticleBody articleBody) {
        textViewTitle.setText(articleBody.getTitle());
        textViewText.setText(Html.fromHtml(articleBody.getText()));
    }

}
