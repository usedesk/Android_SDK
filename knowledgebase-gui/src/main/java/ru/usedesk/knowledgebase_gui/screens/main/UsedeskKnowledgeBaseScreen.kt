package ru.usedesk.knowledgebase_gui.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.*
import ru.usedesk.knowledgebase_gui.IUsedeskOnSearchQueryListener
import ru.usedesk.knowledgebase_gui.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.ToolbarSearchAdapter
import ru.usedesk.knowledgebase_gui.screens.article.ArticlePage
import ru.usedesk.knowledgebase_gui.screens.articles.ArticlesPage
import ru.usedesk.knowledgebase_gui.screens.articles_search.ArticlesSearchPage
import ru.usedesk.knowledgebase_gui.screens.categories.CategoriesPage
import ru.usedesk.knowledgebase_gui.screens.categories.IOnCategoryClickListener
import ru.usedesk.knowledgebase_gui.screens.sections.IOnSectionClickListener
import ru.usedesk.knowledgebase_gui.screens.sections.SectionsPage
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk

class UsedeskKnowledgeBaseScreen : UsedeskFragment(),
        IOnSectionClickListener,
        IOnCategoryClickListener,
        IOnArticleClickListener,
        IUsedeskOnBackPressedListener,
        IUsedeskOnSearchQueryListener,
        IOnTitleChangeListener,
        IUsedeskOnSupportClickListener {

    private val viewModel: KnowledgeBaseViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var toolbarDefaultAdapter: UsedeskToolbarAdapter
    private lateinit var toolbarSearchAdapter: ToolbarSearchAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = inflateItem(inflater,
                container,
                R.layout.usedesk_screen_knowledge_base,
                R.style.Usedesk_KnowledgeBase_Screen) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        toolbarDefaultAdapter = UsedeskToolbarAdapter(requireActivity() as AppCompatActivity,
                binding.toolbar).apply {
            setBackButton {
                requireActivity().onBackPressed()
            }

            setActionButton {
                switchPage(ArticlesSearchPage.newInstance())
            }
        }

        toolbarSearchAdapter = ToolbarSearchAdapter(binding.toolbarSearch, {
            (getLastFragment() as? ArticlesSearchPage)?.onSearchQueryUpdate(it)
        }, {
            onBackPressed()
        })

        viewModel.searchQueryLiveData.observe(viewLifecycleOwner, {
            showSearchQuery(it)
        })

        UsedeskKnowledgeBaseSdk.init(requireContext())

        if (savedInstanceState == null) {
            val sectionsTitle = binding.styleValues
                    .getStyleValues(R.attr.usedesk_knowledgebase_screen_toolbar_title)
                    .getString(R.attr.usedesk_text_1)
            switchPage(SectionsPage.newInstance(), sectionsTitle)
        }
        return binding.rootView
    }

    private fun showSearchQuery(query: String) {
        val fragment = getLastFragment()
        if (fragment is ArticlesSearchPage) {
            fragment.onSearchQueryUpdate(query)
        } else {
            switchPage(ArticlesSearchPage.newInstance())
        }
    }

    override fun onSupportClick() {
        getParentListener<IUsedeskOnSupportClickListener>()?.also {
            it.onSupportClick()
        }
    }

    private fun switchPage(fragment: Fragment,
                           title: String = "") {
        updateToolbar(fragment)
        showInstead(binding.toolbarSearch.rootView,
                binding.toolbar.rootView,
                fragment is ArticlesSearchPage)
        toolbarDefaultAdapter.setTitle(title)
        fragment.arguments = (fragment.arguments ?: Bundle()).apply {
            putString(COMMON_TITLE_KEY, title)
        }
        childFragmentManager.beginTransaction()
                .addToBackStack(fragment.javaClass.name)
                .add(R.id.container, fragment)
                .commit()
    }

    override fun onArticleClick(categoryId: Long,
                                articleId: Long,
                                articleTitle: String) {
        switchPage(ArticlePage.newInstance(categoryId, articleId), articleTitle)
    }

    override fun onCategoryClick(categoryId: Long,
                                 articleTitle: String) {
        switchPage(ArticlesPage.newInstance(categoryId), articleTitle)
    }

    override fun onSectionClick(sectionId: Long,
                                sectionTitle: String) {
        switchPage(CategoriesPage.newInstance(sectionId), sectionTitle)
    }

    override fun onSearchQuery(query: String) {
        if (query.isNotEmpty()) {
            viewModel.onSearchQuery(query)
        }
    }

    override fun onTitle(title: String) {
        binding.toolbar.tvTitle.text = title
    }

    override fun onBackPressed(): Boolean {
        val count = childFragmentManager.fragments.size
        if (count > 1) {
            getLastFragment()?.also {
                childFragmentManager.beginTransaction()
                        .remove(it)
                        .commitNow()
            }
            val lastFragment = getLastFragment()
            val title = if (lastFragment != null) {
                updateToolbar(lastFragment)

                lastFragment.arguments
                        ?.getString(COMMON_TITLE_KEY)
                        ?: ""
            } else {
                ""
            }
            toolbarDefaultAdapter.setTitle(title)
            return true
        }
        return false
    }

    private fun updateToolbar(fragment: Fragment) {
        if (fragment is ArticlesSearchPage) {
            toolbarSearchAdapter.show()
            toolbarDefaultAdapter.hide()
        } else {
            toolbarSearchAdapter.hide()
            toolbarDefaultAdapter.show()
        }
    }

    private fun getLastFragment(): Fragment? {
        return childFragmentManager.fragments.lastOrNull()
    }

    companion object {
        private const val COMMON_TITLE_KEY = "commonTitleKey"

        @JvmStatic
        fun newInstance(): UsedeskKnowledgeBaseScreen {
            return UsedeskKnowledgeBaseScreen()
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val toolbar = UsedeskToolbarAdapter.Binding(rootView.findViewById(R.id.toolbar), defaultStyleId)
        val toolbarSearch = ToolbarSearchAdapter.Binding(rootView.findViewById(R.id.toolbar_search), defaultStyleId)
    }
}