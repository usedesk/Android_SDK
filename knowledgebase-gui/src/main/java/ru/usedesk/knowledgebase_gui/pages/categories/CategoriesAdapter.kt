package ru.usedesk.knowledgebase_gui.pages.categories

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.databinding.UsedeskItemCategoryBinding
import ru.usedesk.knowledgebase_sdk.entity.UsedeskCategory

class CategoriesAdapter internal constructor(
        private val categoryList: List<UsedeskCategory>,
        private val onCategoryClickListener: IOnCategoryClickListener
) : RecyclerView.Adapter<CategoriesAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        return SectionViewHolder(inflateItem(R.layout.usedesk_item_category, viewGroup))
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(categoryList[i], onCategoryClickListener)
    }

    override fun getItemCount(): Int = categoryList.size

    class SectionViewHolder(
            private val binding: UsedeskItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: UsedeskCategory,
                 onCategoryClickListener: IOnCategoryClickListener) {
            binding.tvTitle.text = category.title
            itemView.setOnClickListener {
                onCategoryClickListener.onCategoryClick(category.id)
            }
        }
    }
}