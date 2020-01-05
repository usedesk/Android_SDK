package ru.usedesk.knowledgebase_gui.screens.main.view;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.usedesk.knowledgebase_gui.screens.common.FragmentView;
import ru.usedesk.knowledgebase_gui.screens.helper.FragmentSwitcher;
import ru.usedesk.knowledgebase_gui.screens.main.IOnUsedeskSupportClickListener;
import ru.usedesk.knowledgebase_gui.screens.main.viewmodel.KnowledgeBaseViewModel;
import ru.usedesk.knowledgebase_gui.screens.pages.article.ArticleFragment;
import ru.usedesk.knowledgebase_gui.screens.pages.articlebody.ArticlesBodyFragment;
import ru.usedesk.knowledgebase_gui.screens.pages.articlebody.IOnArticleBodyClickListener;
import ru.usedesk.knowledgebase_gui.screens.pages.articlesinfo.ArticlesInfoFragment;
import ru.usedesk.knowledgebase_gui.screens.pages.articlesinfo.IOnArticleInfoClickListener;
import ru.usedesk.knowledgebase_gui.screens.pages.categories.CategoriesFragment;
import ru.usedesk.knowledgebase_gui.screens.pages.categories.IOnCategoryClickListener;
import ru.usedesk.knowledgebase_gui.screens.pages.sections.IOnSectionClickListener;
import ru.usedesk.knowledgebase_gui.screens.pages.sections.SectionsFragment;
import ru.usedesk.sdk.R;
import ru.usedesk.sdk.external.UsedeskSdk;
import ru.usedesk.sdk.external.ui.IUsedeskOnBackPressedListener;
import ru.usedesk.sdk.external.ui.IUsedeskOnSearchQueryListener;

public class KnowledgeBaseFragment extends FragmentView<KnowledgeBaseViewModel>
        implements IOnSectionClickListener, IOnCategoryClickListener, IOnArticleInfoClickListener,
        IOnArticleBodyClickListener,
        IUsedeskOnBackPressedListener, IUsedeskOnSearchQueryListener {

    public KnowledgeBaseFragment() {
    }

    public static KnowledgeBaseFragment newInstance() {
        return new KnowledgeBaseFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = UsedeskSdk.getUsedeskViewCustomizer()
                .createView(inflater, R.layout.usedesk_fragment_knowledge_base, container, false);

        Button supportButton = view.findViewById(R.id.btn_support);
        supportButton.setOnClickListener(this::onSupportClick);

        initViewModel(new KnowledgeBaseViewModel.Factory(inflater.getContext()));

        getViewModel().getSearchQueryLiveData()
                .observe(this, this::showSearchQuery);

        if (savedInstanceState == null) {
            FragmentSwitcher.switchFragment(this, SectionsFragment.newInstance(),
                    R.id.container);
        }

        return view;
    }

    private void showSearchQuery(@NonNull String query) {
        Fragment fragment = FragmentSwitcher.getLastFragment(this);
        if (fragment instanceof ArticlesBodyFragment) {
            ((ArticlesBodyFragment) fragment).onSearchQueryUpdate(query);
        } else {
            FragmentSwitcher.switchFragment(this, ArticlesBodyFragment.newInstance(query),
                    R.id.container);
        }
    }

    private void onSupportClick(View view) {
        FragmentActivity activity = getActivity();
        if (activity instanceof IOnUsedeskSupportClickListener) {
            ((IOnUsedeskSupportClickListener) activity).onSupportClick();
        }
    }

    private void switchFragment(@NonNull Fragment fragment) {
        FragmentSwitcher.switchFragment(this, fragment, R.id.container);
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
    public void onSearchQuery(String query) {
        if (query != null && !query.isEmpty()) {
            getViewModel().onSearchQuery(query);
        }
    }

    @Override
    public boolean onBackPressed() {
        return FragmentSwitcher.onBackPressed(this);
    }
}
