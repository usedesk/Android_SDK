package ru.usedesk.knowledgebase_gui.screens.pages.article;


import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import ru.usedesk.knowledgebase_gui.R;
import ru.usedesk.knowledgebase_gui.screens.common.FragmentDataView;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBase;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody;

public class ArticleFragment extends FragmentDataView<UsedeskArticleBody, ArticleViewModel> {

    private static final String ARTICLE_ID_KEY = "articleIdKey";

    private final IUsedeskKnowledgeBase usedeskKnowledgeBaseSdk;

    private TextView textViewTitle;
    private TextView textViewText;

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
        textViewText = view.findViewById(R.id.tv_text);
    }

    @Override
    protected ViewModelFactory<ArticleViewModel> getViewModelFactory() {
        long articleId = getNonNullArguments().getLong(ARTICLE_ID_KEY);

        return new ArticleViewModel.Factory(usedeskKnowledgeBaseSdk, articleId);
    }

    @Override
    protected void setDataView(UsedeskArticleBody data) {
        textViewTitle.setText(data.getTitle());
        textViewText.setText(Html.fromHtml(data.getText()));
    }

}
