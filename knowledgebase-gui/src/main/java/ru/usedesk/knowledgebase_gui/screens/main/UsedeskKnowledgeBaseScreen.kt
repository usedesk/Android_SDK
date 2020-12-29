package ru.usedesk.knowledgebase_gui.screens.main

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.common_gui.*
import ru.usedesk.knowledgebase_gui.IUsedeskOnSearchQueryListener
import ru.usedesk.knowledgebase_gui.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.ToolbarSearchAdapter
import ru.usedesk.knowledgebase_gui.screens.article.ArticleFragment
import ru.usedesk.knowledgebase_gui.screens.articles.ArticlesPage
import ru.usedesk.knowledgebase_gui.screens.articles.IOnArticleClickListener
import ru.usedesk.knowledgebase_gui.screens.articles_search.ArticlesSearchPage
import ru.usedesk.knowledgebase_gui.screens.articles_search.IOnArticlesSearchClickListener
import ru.usedesk.knowledgebase_gui.screens.categories.CategoriesPage
import ru.usedesk.knowledgebase_gui.screens.categories.IOnCategoryClickListener
import ru.usedesk.knowledgebase_gui.screens.sections.IOnSectionClickListener
import ru.usedesk.knowledgebase_gui.screens.sections.SectionsPage
import ru.usedesk.knowledgebase_sdk.UsedeskKnowledgeBaseSdk

class UsedeskKnowledgeBaseScreen : UsedeskFragment(),
        IOnSectionClickListener,
        IOnCategoryClickListener,
        IOnArticleClickListener,
        IOnArticlesSearchClickListener,
        IUsedeskOnBackPressedListener,
        IUsedeskOnSearchQueryListener {

    private val viewModel: KnowledgeBaseViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var toolbarAdapter: UsedeskToolbarAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        doInit {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_screen_knowledge_base,
                    R.style.Usedesk_KnowledgeBase_Screen) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }

            binding.btnSupport.setOnClickListener {
                onSupportClick()
            }

            UsedeskKnowledgeBaseSdk.init(requireContext())

            viewModel.searchQueryLiveData.observe(viewLifecycleOwner, {
                showSearchQuery(it)
            })

            binding.toolbar.ivBack.setOnClickListener {
                onBackPressed()
            }

            toolbarAdapter = UsedeskToolbarAdapter(requireActivity() as AppCompatActivity, binding.toolbar)
            toolbarAdapter.setActionButton(R.drawable.ic_search) {
                switchPage(ArticlesSearchPage.newInstance())
            }

            binding.toolbarSearch.etQuery.apply {
                setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        (getLastFragment() as? ArticlesSearchPage)?.onSearchQueryUpdate(text.toString())
                        true
                    } else {
                        false
                    }
                }
            }

            val sectionsTitle = binding.styleValues.getString(R.attr.usedesk_knowledgebase_sections_toolbar_title_text)
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

    private fun onSupportClick() {
        getParentListener<IUsedeskOnSupportClickListener>()?.also {
            it.onSupportClick()
        }
    }

    private fun switchPage(fragment: Fragment,
                           title: String = "") {
        showInstead(binding.toolbarSearch.rootView,
                binding.toolbar.rootView,
                fragment is ArticlesSearchPage)
        toolbarAdapter.setTitle(title)
        fragment.arguments = (fragment.arguments ?: Bundle()).apply {
            putString(COMMON_TITLE_KEY, title)
        }
        childFragmentManager.beginTransaction()
                .addToBackStack(fragment.javaClass.name)
                .replace(R.id.container, fragment)
                .commit()
    }

    override fun onArticleInfoClick(articleId: Long) {
        switchPage(ArticleFragment.newInstance(articleId))
    }

    override fun onArticleClick(articleId: Long) {
        switchPage(ArticleFragment.newInstance(articleId))
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

    override fun onBackPressed(): Boolean {
        val count = childFragmentManager.backStackEntryCount
        if (count > 1) {
            childFragmentManager.popBackStackImmediate()
            val title = getLastFragment()?.arguments
                    ?.getString(COMMON_TITLE_KEY)
                    ?: ""
            toolbarAdapter.setTitle(title)
            return true
        }
        return false
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
        val btnSupport: FloatingActionButton = rootView.findViewById(R.id.fab_support)
        val toolbar = UsedeskToolbarAdapter.Binding(rootView.findViewById(R.id.toolbar), defaultStyleId)
        val toolbarSearch = ToolbarSearchAdapter.Binding(rootView.findViewById(R.id.toolbar_search), defaultStyleId)
    }
}