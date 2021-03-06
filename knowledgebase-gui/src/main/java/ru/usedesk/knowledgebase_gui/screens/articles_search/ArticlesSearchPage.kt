package ru.usedesk.knowledgebase_gui.screens.articles_search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.visibleGone
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.screens.main.IOnArticleClickListener

internal class ArticlesSearchPage : UsedeskFragment() {

    private val viewModel: ArticlesSearchViewModel by viewModels()
    private lateinit var binding: Binding

    private lateinit var articlesSearchAdapter: ArticlesSearchAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        if (savedInstanceState == null) {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_page_list,
                    R.style.Usedesk_KnowledgeBase_Articles_Search_Page) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }.apply {
                tvMessage.text = styleValues
                        .getStyleValues(R.attr.usedesk_knowledgebase_list_page_message_text)
                        .getString(R.attr.usedesk_text_1)

                btnSupport.setOnClickListener {
                    getParentListener<IUsedeskOnSupportClickListener>()?.onSupportClick()
                }

                val withSupportButton = argsGetBoolean(WITH_SUPPORT_BUTTON_KEY, true)
                btnSupport.visibility = visibleGone(withSupportButton)
            }

            articlesSearchAdapter = ArticlesSearchAdapter(binding.rvItems) { articleContent ->
                getParentListener<IOnArticleClickListener>()?.onArticleClick(
                        articleContent.categoryId,
                        articleContent.id,
                        articleContent.title)
            }

            viewModel.onSearchQuery("")
        }

        articlesSearchAdapter.onLiveData(viewModel, viewLifecycleOwner)
        viewModel.articlesLiveData.observe(viewLifecycleOwner) {
            when {
                it == null -> {
                    binding.pbLoading.visibility = View.VISIBLE
                    binding.tvMessage.visibility = View.GONE
                    binding.rvItems.visibility = View.GONE
                }
                it.isEmpty() -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.tvMessage.visibility = View.VISIBLE
                    binding.rvItems.visibility = View.GONE
                }
                else -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.tvMessage.visibility = View.GONE
                    binding.rvItems.visibility = View.VISIBLE
                }
            }
        }

        return binding.rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun onSearchQueryUpdate(searchQuery: String) {
        viewModel.onSearchQuery(searchQuery)
    }

    companion object {
        private const val WITH_SUPPORT_BUTTON_KEY = "withSupportButtonKey"

        fun newInstance(withSupportButton: Boolean = true): ArticlesSearchPage {
            return ArticlesSearchPage().apply {
                arguments = Bundle().apply {
                    putBoolean(WITH_SUPPORT_BUTTON_KEY, withSupportButton)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
        val tvMessage: TextView = rootView.findViewById(R.id.tv_message)
        val btnSupport: FloatingActionButton = rootView.findViewById(R.id.fab_support)
    }
}