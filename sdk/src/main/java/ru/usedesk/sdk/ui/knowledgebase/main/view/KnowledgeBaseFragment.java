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
import ru.usedesk.sdk.ui.knowledgebase.FragmentSwitcher;
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

    private final FragmentSwitcher fragmentSwitcher;

    public KnowledgeBaseFragment() {
        this.fragmentSwitcher = new FragmentSwitcher(this, R.id.container);
    }

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

        fragmentSwitcher.setOnBackStackChangedListener(this::onFragmentStackSize);

        initViewModel(new KnowledgeBaseViewModel.Factory(inflater.getContext()));

        getViewModel().getSearchQueryLiveData()
                .observe(this, this::showSearchQuery);

        if (savedInstanceState == null) {
            fragmentSwitcher.switchFragment(SectionsFragment.newInstance());
        }

        return view;
    }

    private void onFragmentStackSize() {
        if (onFragmentStackSizeListener != null) {
            onFragmentStackSizeListener.onFragmentStackSize(fragmentSwitcher.getStackSize());
        }
    }

    private void showSearchQuery(@NonNull String query) {
        Fragment fragment = fragmentSwitcher.getLastFragment();
        if (fragment instanceof ArticlesBodyFragment) {
            ((ArticlesBodyFragment) fragment).onSearchQueryUpdate(query);
        } else {
            fragmentSwitcher.switchFragment(ArticlesBodyFragment.newInstance(query));
        }
    }

    private void onSupportClick(View view) {
        if (onSupportClickListener != null)
            onSupportClickListener.onSupportClick();
    }

    @Override
    public void onArticleInfoClick(long articleId) {
        fragmentSwitcher.switchFragment(ArticleFragment.newInstance(articleId));
    }

    @Override
    public void onArticleBodyClick(long articleId) {
        fragmentSwitcher.switchFragment(ArticleFragment.newInstance(articleId));
    }

    @Override
    public void onCategoryClick(long categoryId) {
        fragmentSwitcher.switchFragment(ArticlesInfoFragment.newInstance(categoryId));
    }

    @Override
    public void onSectionClick(long sectionId) {
        fragmentSwitcher.switchFragment(CategoriesFragment.newInstance(sectionId));

    }

    @Override
    public void onSearchQuery(@NonNull String query) {
        getViewModel().onSearchQuery(query);
    }

    public boolean onBackPressed() {
        return fragmentSwitcher.onBackPressed();
    }

    public void setOnFragmentStackSizeListener(IOnFragmentStackSizeListener onFragmentStackSizeListener) {
        this.onFragmentStackSizeListener = onFragmentStackSizeListener;
    }

    public void setOnSupportClickListener(IOnSupportClickListener onSupportButtonListener) {
        this.onSupportClickListener = onSupportButtonListener;
    }
}
