package ru.usedesk.knowledgebase_gui.internal.screens.pages.article;


import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import ru.usedesk.knowledgebase_gui.R;
import ru.usedesk.knowledgebase_gui.internal.screens.common.FragmentDataView;
import ru.usedesk.knowledgebase_gui.internal.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody;

public class ArticleFragment extends FragmentDataView<UsedeskArticleBody, ArticleViewModel> {

    private static final String ARTICLE_ID_KEY = "articleIdKey";

    private final IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;

    private TextView textViewTitle;
    private WebView contentWebView;

    public ArticleFragment() {
        super(R.layout.usedesk_fragment_article);

        usedeskKnowledgeBaseSdk = UsedeskKnowledgeBaseSdk.getInstance();
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
        contentWebView = view.findViewById(R.id.wv_content);
        contentWebView.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected ViewModelFactory<ArticleViewModel> getViewModelFactory() {
        long articleId = getNonNullArguments().getLong(ARTICLE_ID_KEY);

        return new ArticleViewModel.Factory(usedeskKnowledgeBaseSdk, articleId);
    }

    @Override
    protected void setDataView(UsedeskArticleBody data) {
        textViewTitle.setText(data.getTitle());
        contentWebView.loadData(data.getText(), "text/html", null);
    }

}
