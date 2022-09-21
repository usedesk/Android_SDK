package ru.usedesk.knowledgebase_gui.screens.articles_search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.*
import ru.usedesk.common_gui.UsedeskCommonViewLoadingAdapter.State
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.article.ArticlePage
import ru.usedesk.knowledgebase_gui.screens.main.KnowledgeBaseViewModel
import ru.usedesk.knowledgebase_gui.screens.main.UsedeskKnowledgeBaseScreen

internal class ArticlesSearchPage : UsedeskFragment() {

    private val parentViewModel: KnowledgeBaseViewModel by viewModels(
        ownerProducer = {
            findParent<UsedeskKnowledgeBaseScreen>() ?: this
        }
    )
    private val viewModel: ArticlesSearchViewModel by viewModels()
    private lateinit var binding: Binding

    private lateinit var articlesSearchAdapter: ArticlesSearchAdapter
    private lateinit var loadingAdapter: UsedeskCommonViewLoadingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_page_list,
            R.style.Usedesk_KnowledgeBase_Articles_Search_Page,
            ::Binding
        ).apply {
            tvMessage.text = styleValues
                .getStyleValues(R.attr.usedesk_knowledgebase_list_page_message_text)
                .getString(R.attr.usedesk_text_1)
        }

        loadingAdapter = UsedeskCommonViewLoadingAdapter(binding.vLoading)

        articlesSearchAdapter = ArticlesSearchAdapter(
            binding.rvItems,
            viewModel,
            lifecycleScope
        ) { articleContent ->
            findNavController().navigate(
                R.id.action_articlesSearchPage_to_articlePage,
                ArticlePage.createBundle(
                    articleContent.title,
                    articleContent.categoryId,
                    articleContent.id
                )
            )
        }

        parentViewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.searchQuery != new.searchQuery) {
                viewModel.onSearchQuery(new.searchQuery)
            }
        }

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.state != new.state) {
                loadingAdapter.update(new.state)
                when (new.state) {
                    State.LOADED -> updateVisible(
                        showMessage = new.articles.isEmpty(),
                        showItems = new.articles.isNotEmpty()
                    )
                    else -> updateVisible()
                }
            }
        }

        return binding.rootView
    }

    private fun updateVisible(
        showMessage: Boolean = false,
        showItems: Boolean = false
    ) {
        binding.tvMessage.visibility = visibleGone(showMessage)
        binding.rvItems.visibility = visibleGone(showItems)
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
        val tvMessage: TextView = rootView.findViewById(R.id.tv_message)
        val vLoading = UsedeskCommonViewLoadingAdapter.Binding(
            rootView.findViewById(R.id.v_loading),
            defaultStyleId
        )
    }
}