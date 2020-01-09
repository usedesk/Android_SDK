package ru.usedesk.knowledgebase_gui.screens.pages.articlesinfo;


import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import ru.usedesk.common_gui.external.UsedeskViewCustomizer;
import ru.usedesk.knowledgebase_gui.screens.common.ViewModelFactory;
import ru.usedesk.knowledgebase_gui.screens.pages.FragmentListView;
import ru.usedesk.knowledgebase_sdk.external.IUsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk;
import ru.usedesk.knowledgebase_sdk.external.entity.ArticleInfo;

public class ArticlesInfoFragment extends FragmentListView<ArticleInfo, ArticlesInfoViewModel> {

    public static final String CATEGORY_ID_KEY = "categoryIdKey";

    private final IUsedeskKnowledgeBaseSdk usedeskKnowledgeBaseSdk;

    public ArticlesInfoFragment() {
        usedeskKnowledgeBaseSdk = UsedeskKnowledgeBaseSdk.getInstance();
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

        return new ArticlesInfoViewModel.Factory(usedeskKnowledgeBaseSdk, categoryId);
    }

    @Override
    protected RecyclerView.Adapter getAdapter(List<ArticleInfo> list) {
        if (!(getParentFragment() instanceof IOnArticleInfoClickListener)) {
            throw new RuntimeException("Parent fragment must implement " +
                    IOnArticleInfoClickListener.class.getSimpleName());
        }

        return new ArticlesInfoAdapter(list, (IOnArticleInfoClickListener) getParentFragment(),
                UsedeskViewCustomizer.getInstance());
    }
}
