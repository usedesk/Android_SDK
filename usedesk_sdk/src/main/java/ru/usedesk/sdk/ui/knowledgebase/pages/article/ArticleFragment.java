package ru.usedesk.sdk.ui.knowledgebase.pages.article;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.ui.knowledgebase.FragmentDataView;
import ru.usedesk.sdk.ui.knowledgebase.ViewModelFactory;

public class ArticleFragment extends FragmentDataView<ArticleBody, ArticleViewModel> {

    private static final String ARTICLE_ID_KEY = "articleIdKey";

    private final KnowledgeBase knowledgeBase;

    private TextView textViewTitle;
    private TextView textViewText;

    public ArticleFragment() {
        super(R.layout.fragment_article);

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
    protected void onView(@NonNull View view) {
        super.onView(view);
        textViewTitle = view.findViewById(R.id.tv_title);
        textViewText = view.findViewById(R.id.tv_text);
    }

    @Override
    protected ViewModelFactory<ArticleViewModel> getViewModelFactory() {
        long articleId = getNonNullArguments().getLong(ARTICLE_ID_KEY);

        return new ArticleViewModel.Factory(knowledgeBase, articleId);
    }

    @Override
    protected void setDataView(ArticleBody data) {
        textViewTitle.setText(data.getTitle());
        textViewText.setText(Html.fromHtml(data.getText()));
    }

}
