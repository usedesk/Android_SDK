package ru.usedesk.knowledgebase_gui.screens.categories

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
import ru.usedesk.knowledgebase_gui.screens.articles.ArticlesPage
import ru.usedesk.knowledgebase_gui.screens.main.UsedeskKnowledgeBaseScreen

internal class CategoriesPage : UsedeskFragment() {

    private val viewModel: CategoriesViewModel by viewModels()

    private lateinit var binding: Binding

    private lateinit var categoriesAdapter: CategoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = inflateItem(
            inflater,
            container,
            R.layout.usedesk_page_list,
            R.style.Usedesk_KnowledgeBase_Categories_Page,
            ::Binding
        )

        argsGetLong(SECTION_ID_KEY)?.also(this@CategoriesPage::init)

        return binding.rootView
    }

    fun init(sectionId: Long) {
        viewModel.init(sectionId)

        categoriesAdapter = CategoriesAdapter(
            binding.rvItems,
            viewModel,
            lifecycleScope
        ) { id, title ->
            findNavController().navigateSafe(
                R.id.dest_categoriesPage,
                R.id.action_categoriesPage_to_articlesPage,
                ArticlesPage.createBundle(title, id)
            )
        }

        viewModel.modelFlow.onEachWithOld { old, new ->
            if (old?.loading != new.loading) {
                showInstead(binding.rvItems, binding.pbLoading, !new.loading)
            }
        }
    }

    companion object {
        private const val SECTION_ID_KEY = "sectionIdKey"

        fun createBundle(title: String, sectionId: Long): Bundle {
            return Bundle().apply {
                putString(UsedeskKnowledgeBaseScreen.COMMON_TITLE_KEY, title)
                putLong(SECTION_ID_KEY, sectionId)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val rvItems: RecyclerView = rootView.findViewById(R.id.rv_items)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
    }
}