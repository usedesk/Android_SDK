package ru.usedesk.knowledgebase_gui.external

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import ru.usedesk.common_gui.external.UsedeskViewCustomizer
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.internal.screens.common.FragmentView
import ru.usedesk.knowledgebase_gui.internal.screens.helper.FragmentSwitcher
import ru.usedesk.knowledgebase_gui.internal.screens.main.KnowledgeBaseViewModel
import ru.usedesk.knowledgebase_gui.internal.screens.pages.article.ArticleFragment
import ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody.ArticlesBodyFragment
import ru.usedesk.knowledgebase_gui.internal.screens.pages.articlebody.IOnArticleBodyClickListener
import ru.usedesk.knowledgebase_gui.internal.screens.pages.articlesinfo.ArticlesInfoFragment
import ru.usedesk.knowledgebase_gui.internal.screens.pages.articlesinfo.IOnArticleInfoClickListener
import ru.usedesk.knowledgebase_gui.internal.screens.pages.categories.CategoriesFragment
import ru.usedesk.knowledgebase_gui.internal.screens.pages.categories.IOnCategoryClickListener
import ru.usedesk.knowledgebase_gui.internal.screens.pages.sections.IOnSectionClickListener
import ru.usedesk.knowledgebase_gui.internal.screens.pages.sections.SectionsFragment

class UsedeskKnowledgeBaseFragment : FragmentView<KnowledgeBaseViewModel?>(), IOnSectionClickListener, IOnCategoryClickListener, IOnArticleInfoClickListener, IOnArticleBodyClickListener, IUsedeskOnBackPressedListener, IUsedeskOnSearchQueryListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = UsedeskViewCustomizer.getInstance()
                .createView(inflater, R.layout.usedesk_fragment_knowledge_base, container, false, R.style.Usedesk_Theme_KnowledgeBase)
        val supportButton = view.findViewById<Button>(R.id.btn_support)
        supportButton.setOnClickListener { view: View -> onSupportClick(view) }
        initViewModel(KnowledgeBaseViewModel.Factory(inflater.context))
        viewModel.searchQueryLiveData
                .observe(viewLifecycleOwner, { query: String -> showSearchQuery(query) })
        if (savedInstanceState == null) {
            FragmentSwitcher.switchFragment(this, SectionsFragment.newInstance(), R.id.container)
        }
        return view
    }

    private fun showSearchQuery(query: String) {
        val fragment = FragmentSwitcher.getLastFragment(this)
        if (fragment is ArticlesBodyFragment) {
            fragment.onSearchQueryUpdate(query)
        } else {
            FragmentSwitcher.switchFragment(this, ArticlesBodyFragment.newInstance(query),
                    R.id.container)
        }
    }

    private fun onSupportClick(view: View) {
        val activity = activity
        if (activity is IUsedeskOnSupportClickListener) {
            (activity as IUsedeskOnSupportClickListener).onSupportClick()
        }
    }

    private fun switchFragment(fragment: Fragment) {
        FragmentSwitcher.switchFragment(this, fragment, R.id.container)
    }

    override fun onArticleInfoClick(articleId: Long) {
        switchFragment(ArticleFragment.newInstance(articleId))
    }

    override fun onArticleBodyClick(articleId: Long) {
        switchFragment(ArticleFragment.newInstance(articleId))
    }

    override fun onCategoryClick(categoryId: Long) {
        switchFragment(ArticlesInfoFragment.newInstance(categoryId))
    }

    override fun onSectionClick(sectionId: Long) {
        switchFragment(CategoriesFragment.newInstance(sectionId))
    }

    override fun onSearchQuery(query: String) {
        if (query != null && !query.isEmpty()) {
            viewModel.onSearchQuery(query)
        }
    }

    override fun onBackPressed(): Boolean {
        return FragmentSwitcher.onBackPressed(this)
    }

    companion object {
        fun newInstance(): UsedeskKnowledgeBaseFragment {
            return UsedeskKnowledgeBaseFragment()
        }
    }
}