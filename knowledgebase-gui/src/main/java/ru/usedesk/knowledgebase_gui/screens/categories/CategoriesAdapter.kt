package ru.usedesk.knowledgebase_gui.screens.categories

import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.IUsedeskAdapter
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

internal class CategoriesAdapter internal constructor(
        recyclerView: RecyclerView,
        private val onCategoryClick: (Long, String) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.SectionViewHolder>(), IUsedeskAdapter<CategoriesViewModel> {

    private var categories = listOf<UsedeskCategory>()

    init {
        recyclerView.adapter = this
    }

    override fun onLiveData(viewModel: CategoriesViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.categoriesLiveData.observe(lifecycleOwner) {
            it?.let {
                if (categories != it) {
                    categories = it
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        return SectionViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_category,
                R.style.Usedesk_KnowledgeBase_Categories_Page_Category) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(categories[i])
    }

    override fun getItemCount(): Int = categories.size

    inner class SectionViewHolder(
            private val binding: Binding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(category: UsedeskCategory) {
            binding.tvTitle.text = category.title
            binding.tvDescription.text = Html.fromHtml(category.description).trim()
            binding.tvCount.text = category.articles.size.toString()
            binding.rootView.setOnClickListener {
                onCategoryClick(category.id, category.title)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val tvDescription: TextView = rootView.findViewById(R.id.tv_description)
        val tvCount: TextView = rootView.findViewById(R.id.tv_count)
    }
}