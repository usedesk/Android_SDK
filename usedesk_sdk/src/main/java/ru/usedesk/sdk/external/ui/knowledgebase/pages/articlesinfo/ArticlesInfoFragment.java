package ru.usedesk.sdk.external.ui.knowledgebase.pages.articlesinfo;


import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import ru.usedesk.sdk.external.UsedeskKnowledgeBase;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.external.ui.knowledgebase.common.ViewModelFactory;
import ru.usedesk.sdk.external.ui.knowledgebase.pages.FragmentListView;

public class ArticlesInfoFragment extends FragmentListView<ArticleInfo, ArticlesInfoViewModel> {

    public static final String CATEGORY_ID_KEY = "categoryIdKey";

    private final UsedeskKnowledgeBase usedeskKnowledgeBase;

    public ArticlesInfoFragment() {
        usedeskKnowledgeBase = UsedeskSdk.getUsedeskKnowledgeBase();
    }

    public static ArticlesInfoFragment newInstance(long categoryId) {
        Bundle args = new Bundle();
        args.putLong(CATEGORY_ID_KEY, categoryId);

        ArticlesInfoFragment articlesInfoFragment = new ArticlesInfoFragment();
        articlesInfoFragment.setArguments(args);
        return articlesInfoFragment;
    }

    @Override
    protected ViewModelFactory<ArticlesInfoViewModel> getViewModelFactory() {
        long categoryId = getNonNullArguments().getLong(CATEGORY_ID_KEY);

        return new ArticlesInfoViewModel.Factory(usedeskKnowledgeBase, categoryId);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<ArticleInfo> list) {
        if (!(getParentFragment() instanceof IOnArticleInfoClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnArticleInfoClickListener.class.getSimpleName());
        }

        return new ArticlesInfoAdapter(list, (IOnArticleInfoClickListener) getParentFragment(),
                usedeskKnowledgeBase.getViewCustomizer());
    }
}
