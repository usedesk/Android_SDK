package ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo;


import android.os.Bundle;

import java.util.List;

import ru.usedesk.sdk.appsdk.KnowledgeBase;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleInfo;
import ru.usedesk.sdk.ui.knowledgebase.pages.FragmentListView;
import ru.usedesk.sdk.ui.knowledgebase.pages.ListViewModel;

public class ArticlesInfoFragment extends FragmentListView<ArticleInfo, ArticlesInfoViewModel> {

    public static final String CATEGORY_ID_KEY = "categoryIdKey";

    private final KnowledgeBase knowledgeBase;

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
    public ListViewModel<ArticleInfo> initViewModel() {
        long categoryId = getNonNullArguments().getLong(CATEGORY_ID_KEY);

        initViewModel(new ArticlesInfoViewModel.Factory(knowledgeBase, categoryId));

        return getViewModel();
    }

    @Override
    protected void onData(List<ArticleInfo> articleInfos) {
        if (!(getParentFragment() instanceof IOnArticleInfoClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnArticleInfoClickListener.class.getSimpleName());
        }
        initRecyclerView(new ArticlesInfoAdapter(articleInfos,
                (IOnArticleInfoClickListener) getParentFragment()));
    }
}
