package ru.usedesk.knowledgebase_gui.pages.categories

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

internal class CategoriesAdapter internal constructor(
        private val categoryList: List<UsedeskCategory>,
        private val onCategoryClickListener: IOnCategoryClickListener
) : RecyclerView.Adapter<CategoriesAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        return SectionViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_category,
                R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
            CategoryBinding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(categoryList[i], onCategoryClickListener)
    }

    override fun getItemCount(): Int = categoryList.size

    class SectionViewHolder(
            private val binding: CategoryBinding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(category: UsedeskCategory,
                 onCategoryClickListener: IOnCategoryClickListener) {
            binding.tvTitle.text = category.title
            itemView.setOnClickListener {
                onCategoryClickListener.onCategoryClick(category.id)
            }
        }
    }

    internal class CategoryBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }
}