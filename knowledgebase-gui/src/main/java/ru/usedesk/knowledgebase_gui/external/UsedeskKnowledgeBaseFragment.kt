package ru.usedesk.knowledgebase_gui.external

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.usedesk.common_gui.internal.inflateBinding
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.databinding.UsedeskFragmentKnowledgeBaseBinding
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

    private lateinit var binding: UsedeskFragmentKnowledgeBaseBinding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = inflateBinding(inflater,
                container,
                R.layout.usedesk_fragment_knowledge_base,
                R.style.Usedesk_Theme_KnowledgeBase)

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
        return binding.root
    }

    private fun showSearchQuery(query: String) {
        val fragment = FragmentSwitcher.getLastFragment(this)
        if (fragment is ArticlesBodyFragment) {
            fragment.onSearchQueryUpdate(query)
        } else {
            switchFragment(ArticlesBodyFragment.newInstance(query))
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
}