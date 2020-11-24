package ru.usedesk.knowledgebase_gui.internal.screens.pages.categories

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.external.IUsedeskViewCustomizer
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskCategory

class CategoriesAdapter internal constructor(private val categoryList: List<UsedeskCategory>,
                                             private val onCategoryClickListener: IOnCategoryClickListener,
                                             private val usedeskViewCustomizer: IUsedeskViewCustomizer) : RecyclerView.Adapter<CategoriesAdapter.SectionViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        val view = usedeskViewCustomizer.createView(viewGroup, R.layout.usedesk_item_category, R.style.Usedesk_Theme_KnowledgeBase)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(categoryList[i], onCategoryClickListener)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    internal class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView
        fun bind(category: UsedeskCategory,
                 onCategoryClickListener: IOnCategoryClickListener) {
            textViewTitle.text = category.title
            itemView.setOnClickListener { v: View? -> onCategoryClickListener.onCategoryClick(category.id) }
        }

        init {
            textViewTitle = itemView.findViewById(R.id.tv_title)
        }
    }
}