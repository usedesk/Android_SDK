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

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
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

        if (savedInstanceState == null) {
            argsGetLong(CATEGORY_ID_KEY)?.also { categoryId ->
                init(categoryId)
            }
        }

        return binding.rootView
    }

    fun init(categoryId: Long) {
        viewModel.init(categoryId)

        ArticlesAdapter(binding.rvItems,
                viewLifecycleOwner,
                viewModel) { articleInfo ->
            getParentListener<IOnArticleClickListener>()?.onArticleClick(
                    articleInfo.categoryId,
                    articleInfo.id,
                    articleInfo.title)
        }

        showInstead(binding.pbLoading, binding.rvItems)
        viewModel.articleInfoListLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                showInstead(binding.rvItems, binding.pbLoading)
            }
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