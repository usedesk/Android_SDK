package ru.usedesk.knowledgebase_gui.pages.articlesinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.UsedeskFragment
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R

internal class ArticlesInfoFragment : UsedeskFragment() {

    private val viewModel: ArticlesInfoViewModel by viewModels()

    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = inflateItem(inflater,
                container,
                R.layout.usedesk_fragment_list,
                R.style.Usedesk_KnowledgeBase
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }

        val categoryId = argsGetLong(CATEGORY_ID_KEY)
        if (categoryId != null) {
            init(categoryId)
        }

        return binding.rootView
    }

    fun init(categoryId: Long) {
        viewModel.init(categoryId)

        ArticlesInfoAdapter(binding.rvItems,
                viewLifecycleOwner,
                viewModel) {
            getParentListener<IOnArticleInfoClickListener>()?.onArticleInfoClick(it)
        }
    }

    companion object {
        private const val CATEGORY_ID_KEY = "categoryIdKey"

        fun newInstance(categoryId: Long): ArticlesInfoFragment {
            return ArticlesInfoFragment().apply {
                arguments = Bundle().apply {
                    putLong(CATEGORY_ID_KEY, categoryId)
                }
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
    }
}