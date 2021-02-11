package ru.usedesk.knowledgebase_gui.screens.articles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.showInstead
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.screens.IUsedeskOnSupportClickListener
import ru.usedesk.knowledgebase_gui.screens.main.IOnArticleClickListener

internal class ArticlesPage : UsedeskFragment() {

    private val viewModel: ArticlesViewModel by viewModels()

    private lateinit var binding: Binding

    private lateinit var articlesAdapter: ArticlesAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        if (savedInstanceState == null) {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_page_list,
                    R.style.Usedesk_KnowledgeBase_Articles_Page
            ) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }.apply {
                btnSupport.setOnClickListener {
                    getParentListener<IUsedeskOnSupportClickListener>()?.onSupportClick()
                }
            }

            argsGetLong(CATEGORY_ID_KEY)?.also { categoryId ->
                init(categoryId)
            }
        }

        articlesAdapter.onLiveData(viewModel, viewLifecycleOwner)
        viewModel.articleInfoListLiveData.observe(viewLifecycleOwner) {
            showInstead(binding.rvItems, binding.pbLoading, it != null)
        }

        return binding.rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun init(categoryId: Long) {
        viewModel.init(categoryId)

        articlesAdapter = ArticlesAdapter(binding.rvItems) { articleInfo ->
            getParentListener<IOnArticleClickListener>()?.onArticleClick(
                    articleInfo.categoryId,
                    articleInfo.id,
                    articleInfo.title)
        }
    }

    companion object {
        private const val CATEGORY_ID_KEY = "categoryIdKey"

        fun newInstance(categoryId: Long): ArticlesPage {
            return ArticlesPage().apply {
                arguments = Bundle().apply {
                    putLong(CATEGORY_ID_KEY, categoryId)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
        val btnSupport: FloatingActionButton = rootView.findViewById(R.id.fab_support)
    }
}