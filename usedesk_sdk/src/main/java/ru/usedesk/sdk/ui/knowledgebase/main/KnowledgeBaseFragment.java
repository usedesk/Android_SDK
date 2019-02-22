package ru.usedesk.sdk.ui.knowledgebase.main;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.ui.knowledgebase.FragmentView;
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

    private View pagesView;
    private View searchView;
    private Button supportButton;

    public static KnowledgeBaseFragment newInstance() {
        return new KnowledgeBaseFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_knowledge_base, container, false);

        pagesView = view.findViewById(R.id.pages_layout);
        searchView = view.findViewById(R.id.search_layout);
        supportButton = view.findViewById(R.id.btn_support);

        supportButton.setOnClickListener(this::onSupportClick);

        hideSearchFragment();

        initViewModel(new KnowledgeBaseViewModel.Factory(inflater.getContext()));

        getViewModel().getSearchQueryLiveData()
                .observe(this, this::showSearchQuery);

        if (savedInstanceState == null) {
            switchPage(SectionsFragment.newInstance());
        }

        return view;
    }

    private void showSearchQuery(String query) {
        if (query.isEmpty()) {
            hideSearchFragment();
        } else {
            showSearchFragment(query);
        }
    }

    private void onSupportClick(View view) {
        IOnSupportClickListener onSupportClickListener;
        if (getParentFragment() instanceof IOnSupportClickListener) {
            onSupportClickListener = (IOnSupportClickListener) getParentFragment();
        } else if (getActivity() instanceof IOnSupportClickListener) {
            onSupportClickListener = (IOnSupportClickListener) getActivity();
        } else {
            throw new RuntimeException("Parent must to implement " + IOnSupportClickListener.class.getSimpleName());
        }

        onSupportClickListener.onSupportClick();
    }

    private void switchPage(@NonNull Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.page_container, fragment)
                .commit();
    }

    private void switchSearch(@NonNull Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.search_container, fragment)
                .commit();
    }

    @Override
    public void onArticleInfoClick(long articleId) {
        switchPage(ArticleFragment.newInstance(articleId));
    }

    @Override
    public void onArticleBodyClick(long articleId) {
        switchSearch(ArticleFragment.newInstance(articleId));
    }

    @Override
    public void onCategoryClick(long categoryId) {
        switchPage(ArticlesInfoFragment.newInstance(categoryId));
    }

    @Override
    public void onSectionClick(long sectionId) {
        switchPage(CategoriesFragment.newInstance(sectionId));

    }

    @Override
    public void onSearchQuery(@NonNull String query) {
        getViewModel().onSearchQuery(query);
    }

    /**
     * Возвращает false если нажатие не обработано
     **/
    public boolean onBackPressed() {
        if (searchView.getVisibility() == View.VISIBLE) {
            getChildFragmentManager().findFragmentById(R.id.search_container);
        }
        return false;
    }

    private void showSearchFragment(@NonNull String query) {
        pagesView.setVisibility(View.GONE);
        searchView.setVisibility(View.VISIBLE);

        switchSearch(ArticlesBodyFragment.newInstance(query));
    }

    private void hideSearchFragment() {
        pagesView.setVisibility(View.VISIBLE);
        searchView.setVisibility(View.GONE);
    }
}
