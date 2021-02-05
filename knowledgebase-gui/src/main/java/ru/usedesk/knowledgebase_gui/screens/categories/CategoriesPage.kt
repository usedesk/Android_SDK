package ru.usedesk.knowledgebase_gui.screens.categories

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

internal class CategoriesPage : UsedeskFragment() {

    private val viewModel: CategoriesViewModel by viewModels()

    private lateinit var binding: Binding

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        if (savedInstanceState == null) {
            binding = inflateItem(inflater,
                    container,
                    R.layout.usedesk_page_list,
                    R.style.Usedesk_KnowledgeBase_Categories_Page) { rootView, defaultStyleId ->
                Binding(rootView, defaultStyleId)
            }.apply {
                btnSupport.setOnClickListener {
                    getParentListener<IUsedeskOnSupportClickListener>()?.onSupportClick()
                }
            }

            argsGetLong(SECTION_ID_KEY)?.also { sectionId ->
                init(sectionId)
            }
        }

        return binding.rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    fun init(sectionId: Long) {
        viewModel.init(sectionId)

        CategoriesAdapter(binding.rvItems,
                viewLifecycleOwner,
                viewModel) { id, title ->
            getParentListener<IOnCategoryClickListener>()?.onCategoryClick(id, title)
        }

        showInstead(binding.pbLoading, binding.rvItems)
        viewModel.categoriesLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                showInstead(binding.rvItems, binding.pbLoading)
            }
        }
    }

    companion object {
        private const val SECTION_ID_KEY = "sectionIdKey"

        fun newInstance(sectionId: Long): CategoriesPage {
            return CategoriesPage().apply {
                arguments = Bundle().apply {
                    putLong(SECTION_ID_KEY, sectionId)
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