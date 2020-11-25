package ru.usedesk.knowledgebase_gui.external

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.internal.argsGetInt
import ru.usedesk.common_gui.internal.inflateFragment
import ru.usedesk.knowledgebase_gui.R
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
import ru.usedesk.knowledgebase_sdk.external.UsedeskKnowledgeBaseSdk

class UsedeskKnowledgeBaseFragment : Fragment(),
        IOnSectionClickListener,
        IOnCategoryClickListener,
        IOnArticleInfoClickListener,
        IOnArticleBodyClickListener,
        IUsedeskOnBackPressedListener,
        IUsedeskOnSearchQueryListener {

    private val viewModel: KnowledgeBaseViewModel by viewModels()

    private var themeId: Int = R.style.Usedesk_Theme_KnowledgeBase

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        themeId = argsGetInt(arguments, THEME_ID_KEY, R.style.Usedesk_Theme_KnowledgeBase)

        val rootView = inflateFragment(inflater, container, themeId, R.layout.usedesk_fragment_knowledge_base)

        rootView.findViewById<View>(R.id.btn_support).setOnClickListener {
            onSupportClick()
        }

        UsedeskKnowledgeBaseSdk.init(requireContext())

        viewModel.searchQueryLiveData.observe(viewLifecycleOwner, {
            showSearchQuery(it)
        })

        if (savedInstanceState == null) {
            switchFragment(SectionsFragment.newInstance(themeId))
        }
        return rootView
    }

    private fun showSearchQuery(query: String) {
        val fragment = FragmentSwitcher.getLastFragment(this)
        if (fragment is ArticlesBodyFragment) {
            fragment.onSearchQueryUpdate(query)
        } else {
            switchFragment(ArticlesBodyFragment.newInstance(null, query))
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
        switchFragment(ArticleFragment.newInstance(themeId, articleId))
    }

    override fun onArticleBodyClick(articleId: Long) {
        switchFragment(ArticleFragment.newInstance(themeId, articleId))
    }

    override fun onCategoryClick(categoryId: Long) {
        switchFragment(ArticlesInfoFragment.newInstance(themeId, categoryId))
    }

    override fun onSectionClick(sectionId: Long) {
        switchFragment(CategoriesFragment.newInstance(themeId, sectionId))
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
        private const val THEME_ID_KEY = "themeIdKey"

        @JvmOverloads
        @JvmStatic
        fun newInstance(themeId: Int? = null): UsedeskKnowledgeBaseFragment {
            return UsedeskKnowledgeBaseFragment().apply {
                arguments = Bundle().apply {
                    if (themeId != null) {
                        putInt(THEME_ID_KEY, themeId)
                    }
                }
            }
        }
    }
}