package ru.usedesk.knowledgebase_gui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.IUsedeskOnBackPressedListener
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.IUsedeskOnSearchQueryListener
import ru.usedesk.knowledgebase_gui.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.helper.FragmentSwitcher
import ru.usedesk.knowledgebase_gui.pages.article.ArticleFragment
import ru.usedesk.knowledgebase_gui.pages.articles.ArticlesFragment
import ru.usedesk.knowledgebase_gui.pages.articles.IOnArticleClickListener
import ru.usedesk.knowledgebase_gui.pages.articles_search.ArticlesSearchFragment
import ru.usedesk.knowledgebase_gui.pages.articles_search.IOnArticlesSearchClickListener
import ru.usedesk.knowledgebase_gui.pages.categories.CategoriesFragment
import ru.usedesk.knowledgebase_gui.pages.categories.IOnCategoryClickListener
import ru.usedesk.knowledgebase_gui.pages.sections.IOnSectionClickListener
import ru.usedesk.knowledgebase_gui.pages.sections.SectionsFragment
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk

class UsedeskKnowledgeBaseFragment : UsedeskFragment(),
        IOnSectionClickListener,
        IOnCategoryClickListener,
        IOnArticleClickListener,
        IOnArticlesSearchClickListener,
        IUsedeskOnBackPressedListener,
        IUsedeskOnSearchQueryListener {

    private val viewModel: KnowledgeBaseViewModel by viewModels()

    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = inflateItem(inflater,
                container,
                R.layout.usedesk_fragment_knowledge_base,
                R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        binding.btnSupport.setOnClickListener {
            onSupportClick()
        }

        UsedeskKnowledgeBaseSdk.init(requireContext())

        viewModel.searchQueryLiveData.observe(viewLifecycleOwner, {
            showSearchQuery(it)
        })

        if (savedInstanceState == null) {
            switchFragment(SectionsFragment.newInstance())
        }
        return binding.rootView
    }

    private fun showSearchQuery(query: String) {
        val fragment = FragmentSwitcher.getLastFragment(this)
        if (fragment is ArticlesSearchFragment) {
            fragment.onSearchQueryUpdate(query)
        } else {
            switchFragment(ArticlesSearchFragment.newInstance(query))
        }
    }

    private fun onSupportClick() {
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

    override fun onArticleClick(articleId: Long) {
        switchFragment(ArticleFragment.newInstance(articleId))
    }

    override fun onCategoryClick(categoryId: Long) {
        switchFragment(ArticlesFragment.newInstance(categoryId))
    }

    override fun onSectionClick(sectionId: Long) {
        switchFragment(CategoriesFragment.newInstance(sectionId))
    }

    override fun onSearchQuery(query: String) {
        if (query.isNotEmpty()) {
            viewModel.onSearchQuery(query)
        }
    }

    override fun onBackPressed(): Boolean {
        return FragmentSwitcher.onBackPressed(this)
    }

    companion object {
        @JvmStatic
        fun newInstance(): UsedeskKnowledgeBaseFragment {
            return UsedeskKnowledgeBaseFragment()
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val btnSupport: Button = rootView.findViewById(R.id.btn_support)
    }
}