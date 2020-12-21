package ru.usedesk.knowledgebase_gui.pages.categories

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

internal class CategoriesAdapter internal constructor(
        recyclerView: RecyclerView,
        lifecycleOwner: LifecycleOwner,
        private val viewModel: CategoriesViewModel,
        private val onCategoryClick: (Long) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.SectionViewHolder>() {

    private var categoryList = listOf<UsedeskCategory>()

    init {
        recyclerView.adapter = this

        viewModel.categoriesLiveData.observe(lifecycleOwner) {
            categoryList = it
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        return SectionViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_category,
                R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
            CategoryBinding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(categoryList[i])
    }

    override fun getItemCount(): Int = categoryList.size

    inner class SectionViewHolder(
            private val binding: CategoryBinding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(category: UsedeskCategory) {
            binding.tvTitle.text = category.title
            binding.tvDescription.text = category.title//TODO: тут в сущности, видимо, должно быть поле с описанием
            binding.rootView.setOnClickListener {
                onCategoryClick(category.id)
            }
        }
    }

    internal class CategoryBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val tvDescription: TextView = rootView.findViewById(R.id.tv_description)
        val tvCount: TextView = rootView.findViewById(R.id.tv_count)
    }
}