package ru.usedesk.sdk.ui.knowledgebase.main;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.usedesk.sdk.R;
import ru.usedesk.sdk.ui.knowledgebase.pages.article.ArticleFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo.ArticlesInfoFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.articlesinfo.IOnArticleInfoClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.categories.CategoriesFragment;
import ru.usedesk.sdk.ui.knowledgebase.pages.categories.IOnCategoryClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.sections.IOnSectionClickListener;
import ru.usedesk.sdk.ui.knowledgebase.pages.sections.SectionsFragment;

public class KnowledgeBaseFragment extends Fragment implements IOnSectionClickListener,
        IOnCategoryClickListener, IOnArticleInfoClickListener {

    public KnowledgeBaseFragment() {
    }

    public static KnowledgeBaseFragment newInstance() {
        return new KnowledgeBaseFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_knowledge_base, container, false);

        ViewModelProviders.of(this, KnowledgeBaseViewModel.getFactory(inflater.getContext()))
                .get(KnowledgeBaseViewModel.class);

        if (savedInstanceState == null) {
            switchFragment(SectionsFragment.newInstance());
        }

        return view;
    }

    private void switchFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onArticleClick(long articleId) {
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
}
