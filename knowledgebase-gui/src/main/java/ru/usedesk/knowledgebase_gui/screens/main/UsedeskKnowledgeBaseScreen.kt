package ru.usedesk.knowledgebase_gui.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.*
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
    IOnTitleChangeListener {

    private val viewModel: KnowledgeBaseViewModel by viewModels()

    private lateinit var binding: Binding
    private lateinit var toolbarDefaultAdapter: UsedeskToolbarAdapter
    private lateinit var toolbarSearchAdapter: ToolbarSearchAdapter

    private var withSupportButton = true
    private var withArticleRating = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_screen_knowledge_base,
            R.style.Usedesk_KnowledgeBase_Screen
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        withSupportButton = argsGetBoolean(WITH_SUPPORT_BUTTON_KEY, withSupportButton)
        withArticleRating = argsGetBoolean(WITH_ARTICLE_RATING_KEY, withArticleRating)

        toolbarDefaultAdapter = UsedeskToolbarAdapter(
            requireActivity() as AppCompatActivity,
            binding.toolbar
        ).apply {
            setBackButton {
                requireActivity().onBackPressed()
            }

            setActionButton {
                switchPage(ArticlesSearchPage.newInstance(withSupportButton))
            }
        }

        toolbarSearchAdapter = ToolbarSearchAdapter(binding.toolbarSearch, {
            (getLastFragment() as? ArticlesSearchPage)?.onSearchQueryUpdate(it)
        }, {
            onBackPressed()
        })

        UsedeskKnowledgeBaseSdk.init(requireContext())

        if (childFragmentManager.backStackEntryCount == 0) {
            val sectionsTitle = binding.styleValues
                .getStyleValues(R.attr.usedesk_common_toolbar_title_text)
                .getString(R.attr.usedesk_text_1)
            val fragment = SectionsPage.newInstance(withSupportButton)
            switchPage(fragment, sectionsTitle)
        } else {
            getLastFragment()?.let {
                updateToolbar(it)
                val title = it.arguments?.getString(COMMON_TITLE_KEY)
                toolbarDefaultAdapter.setTitle(title)
            }
        }

        hideKeyboard(binding.rootView)

        viewModel.searchQueryLiveData.observe(viewLifecycleOwner, {
            it?.let {
                showSearchQuery(it)
            }
        })

        return binding.rootView
    }

    private fun showSearchQuery(query: String) {
        val fragment = getLastFragment()
        if (fragment is ArticlesSearchPage) {
            fragment.onSearchQueryUpdate(query)
        } else {
            switchPage(ArticlesSearchPage.newInstance(withSupportButton))
        }
    }

    private fun switchPage(
        fragment: Fragment,
        title: String = ""
    ) {
        updateToolbar(fragment)
        toolbarDefaultAdapter.setTitle(title)
        fragment.arguments = (fragment.arguments ?: Bundle()).apply {
            putString(COMMON_TITLE_KEY, title)
        }
        childFragmentManager.beginTransaction()
            .addToBackStack(fragment.javaClass.name + ":" + fragment.hashCode())
            .replace(R.id.container, fragment)
            .commit()
    }

    override fun onArticleClick(
        categoryId: Long,
        articleId: Long,
        articleTitle: String
    ) {
        val fragment = ArticlePage.newInstance(
            withSupportButton,
            withArticleRating,
            categoryId,
            articleId
        )
        switchPage(fragment, articleTitle)
    }

    override fun onCategoryClick(
        categoryId: Long,
        articleTitle: String
    ) {
        val fragment = ArticlesPage.newInstance(withSupportButton, categoryId)
        switchPage(fragment, articleTitle)
    }

    override fun onSectionClick(
        sectionId: Long,
        sectionTitle: String
    ) {
        val fragment = CategoriesPage.newInstance(withSupportButton, sectionId)
        switchPage(fragment, sectionTitle)
    }

    override fun onSearchQuery(query: String) {
        if (query.isNotEmpty()) {
            viewModel.onSearchQuery(query)
        }
    }

    override fun onTitle(title: String) {
        binding.toolbar.tvTitle.text = title
        getLastFragment()?.apply {
            (arguments ?: Bundle()).putString(COMMON_TITLE_KEY, title)
        }
    }

    override fun onBackPressed(): Boolean {
        if (childFragmentManager.backStackEntryCount > 1) {
            childFragmentManager.popBackStackImmediate()
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
            showKeyboard(binding.toolbarSearch.etQuery)
        } else {
            toolbarSearchAdapter.hide()
            toolbarDefaultAdapter.show()
            hideKeyboard(binding.rootView)
        }
    }

    private fun getLastFragment(): Fragment? {
        return childFragmentManager.fragments.lastOrNull()
    }

    companion object {
        private const val COMMON_TITLE_KEY = "commonTitleKey"
        private const val WITH_SUPPORT_BUTTON_KEY = "withSupportButtonKey"
        private const val WITH_ARTICLE_RATING_KEY = "withArticleRatingKey"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            withSupportButton: Boolean = true,
            withArticleRating: Boolean = true
        ): UsedeskKnowledgeBaseScreen {
            return UsedeskKnowledgeBaseScreen().apply {
                arguments = Bundle().apply {
                    putBoolean(WITH_SUPPORT_BUTTON_KEY, withSupportButton)
                    putBoolean(WITH_ARTICLE_RATING_KEY, withArticleRating)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val toolbar =
            UsedeskToolbarAdapter.Binding(rootView.findViewById(R.id.toolbar), defaultStyleId)
        val toolbarSearch =
            ToolbarSearchAdapter.Binding(rootView.findViewById(R.id.toolbar_search), defaultStyleId)
    }
}