package ru.usedesk.sdk.external.ui.knowledgebase.pages.article;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.UsedeskKnowledgeBase;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.external.ui.knowledgebase.common.FragmentDataView;
import ru.usedesk.sdk.external.ui.knowledgebase.common.ViewModelFactory;

public class ArticleFragment extends FragmentDataView<ArticleBody, ArticleViewModel> {

    private static final String ARTICLE_ID_KEY = "articleIdKey";

    private final UsedeskKnowledgeBase usedeskKnowledgeBase;

    private TextView textViewTitle;
    private TextView textViewText;

    public ArticleFragment() {
        super(UsedeskSdk.getUsedeskKnowledgeBase().getViewCustomizer().getLayoutId(R.layout.usedesk_fragment_article));

        usedeskKnowledgeBase = UsedeskSdk.getUsedeskKnowledgeBase();
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

        return new ArticleViewModel.Factory(usedeskKnowledgeBase, articleId);
    }

    @Override
    protected void setDataView(ArticleBody data) {
        textViewTitle.setText(data.getTitle());
        textViewText.setText(Html.fromHtml(data.getText()));
    }

}
