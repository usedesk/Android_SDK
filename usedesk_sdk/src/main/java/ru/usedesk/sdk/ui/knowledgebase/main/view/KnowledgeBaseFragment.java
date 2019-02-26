package ru.usedesk.sdk.ui.knowledgebase.main.view;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.domain.entity.knowledgebase.KnowledgeBaseConfiguration;
import ru.usedesk.sdk.ui.knowledgebase.FragmentView;
import ru.usedesk.sdk.ui.knowledgebase.main.IOnFragmentStackSizeListener;
import ru.usedesk.sdk.ui.knowledgebase.main.IOnSearchQueryListener;
import ru.usedesk.sdk.ui.knowledgebase.main.IOnSupportClickListener;
import ru.usedesk.sdk.ui.knowledgebase.main.viewmodel.KnowledgeBaseViewModel;
import ru.usedesk.sdk.ui.knowledgebase.pages.article.ArticleFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.articlebody.ArticlesBodyFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.articlebody.IOnArticleBodyClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo.ArticlesInfoFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo.IOnArticleInfoClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.categories.CategoriesFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.categories.IOnCategoryClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.sections.IOnSectionClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.sections.SectionsFragment;

public class KnowledgeBaseFragment extends FragmentView<KnowledgeBaseViewModel>
        implements IOnSectionClickListener, IOnCategoryClickListener, IOnArticleInfoClickListener,
        IOnArticleBodyClickListener, IOnSearchQueryListener {
    private static final String COMPANY_ID_KEY = "companyIdKey";
    private static final String TOKEN_KEY = "tokenKey";

    private IOnFragmentStackSizeListener onFragmentStackSizeListener;
    private IOnSupportClickListener onSupportClickListener;

    public static KnowledgeBaseFragment newInstance() {
        return new KnowledgeBaseFragment();
    }

    private static KnowledgeBaseConfiguration getConfiguration(Bundle args) {
        if (args != null) {
            String companyId = args.getString(COMPANY_ID_KEY);
            String token = args.getString(TOKEN_KEY);
            if (companyId != null && token != null) {
                return new KnowledgeBaseConfiguration(companyId, token);
            }
        }
        return null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_knowledge_base, container, false);

        Button supportButton = view.findViewById(R.id.btn_support);
        supportButton.setOnClickListener(this::onSupportClick);

        getChildFragmentManager().removeOnBackStackChangedListener(this::onFragmentStackSize);
        getChildFragmentManager().addOnBackStackChangedListener(this::onFragmentStackSize);

        initViewModel(new KnowledgeBaseViewModel.Factory(inflater.getContext()));

        getViewModel().getSearchQueryLiveData()
                .observe(this, this::showSearchQuery);

        if (savedInstanceState == null) {
            switchFragment(SectionsFragment.newInstance());
        }

        return view;
    }

    private void onFragmentStackSize() {
        if (onFragmentStackSizeListener != null) {
            onFragmentStackSizeListener.onFragmentStackSize(
                    getChildFragmentManager().getBackStackEntryCount());
        }
    }

    private void showSearchQuery(String query) {
        switchFragment(ArticlesBodyFragment.newInstance(query));
    }

    private void onSupportClick(View view) {
        if (onSupportClickListener != null)
            onSupportClickListener.onSupportClick();
    }

    private void switchFragment(@NonNull Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .addToBackStack("cur")
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onArticleInfoClick(long articleId) {
        switchFragment(ArticleFragment.newInstance(articleId));
    }

    @Override
    public void onArticleBodyClick(long articleId) {
        switchFragment(ArticleFragment.newInstance(articleId));
    }

    @Override
    public void onCategoryClick(long categoryId) {
        switchFragment(ArticlesInfoFragment.newInstance(categoryId));
    }

    @Override
    public void onSectionClick(long sectionId) {
        switchFragment(CategoriesFragment.newInstance(sectionId));

    }

    @Override
    public void onSearchQuery(@NonNull String query) {
        getViewModel().onSearchQuery(query);
    }

    public boolean onBackPressed() {
        if (getChildFragmentManager().getBackStackEntryCount() > 1) {
            getChildFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    public void setOnFragmentStackSizeListener(IOnFragmentStackSizeListener onFragmentStackSizeListener) {
        this.onFragmentStackSizeListener = onFragmentStackSizeListener;
    }

    public void setOnSupportClickListener(IOnSupportClickListener onSupportButtonListener) {
        this.onSupportClickListener = onSupportButtonListener;
    }
}
