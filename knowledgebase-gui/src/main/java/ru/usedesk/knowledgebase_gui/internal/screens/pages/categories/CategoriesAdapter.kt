package ru.usedesk.knowledgebase_gui.internal.screens.pages.categories

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.internal.inflateItem
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory

class CategoriesAdapter internal constructor(
        private val categoryList: List<UsedeskCategory>,
        private val onCategoryClickListener: IOnCategoryClickListener
) : RecyclerView.Adapter<CategoriesAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        val view: View = inflateItem(R.layout.usedesk_item_category, viewGroup)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(categoryList[i], onCategoryClickListener)
    }

    override fun getItemCount(): Int = categoryList.size


    class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.tv_title)

        fun bind(category: UsedeskCategory,
                 onCategoryClickListener: IOnCategoryClickListener) {
            textViewTitle.text = category.title
            itemView.setOnClickListener {
                onCategoryClickListener.onCategoryClick(category.id)
            }
        }
    }
}