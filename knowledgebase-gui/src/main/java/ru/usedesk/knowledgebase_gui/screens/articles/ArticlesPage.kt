package ru.usedesk.knowledgebase_gui.screens.articles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.showInstead
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.article.ArticlePage
import ru.usedesk.knowledgebase_gui.screens.main.UsedeskKnowledgeBaseScreen

internal class ArticlesPage : UsedeskFragment() {

    private val viewModel: ArticlesViewModel by viewModels()

    private lateinit var binding: Binding

    private lateinit var articlesAdapter: ArticlesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_page_list,
            R.style.Usedesk_KnowledgeBase_Articles_Page,
            ::Binding
        )

        argsGetLong(CATEGORY_ID_KEY)?.also(this@ArticlesPage::init)

        return binding.rootView
    }

    fun init(categoryId: Long) {
        viewModel.init(categoryId)

        articlesAdapter = ArticlesAdapter(
            binding.rvItems,
            viewModel,
            lifecycleScope
        ) { articleInfo ->
            findNavController().navigate(
                R.id.action_articlesPage_to_articlePage,
                ArticlePage.createBundle(
                    articleInfo.title,
                    articleInfo.categoryId,
                    articleInfo.id
                )
            )
        }

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.loading != new.loading) {
                showInstead(binding.rvItems, binding.pbLoading, !new.loading)
            }
        }
    }

    companion object {
        private const val CATEGORY_ID_KEY = "categoryIdKey"

        fun createBundle(title: String, categoryId: Long): Bundle {
            return Bundle().apply {
                putString(UsedeskKnowledgeBaseScreen.COMMON_TITLE_KEY, title)
                putLong(CATEGORY_ID_KEY, categoryId)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
    }
}