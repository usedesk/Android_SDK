package ru.usedesk.knowledgebase_gui.internal.screens.pages.sections

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.internal.inflateItem
import ru.usedesk.common_gui.internal.setImage
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection

class SectionsAdapter(
        private val sectionList: List<UsedeskSection>,
        private val onSectionClickListener: IOnSectionClickListener
) : RecyclerView.Adapter<SectionsAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        val itemView: ViewGroup = inflateItem(R.layout.usedesk_item_section, viewGroup)
        return SectionViewHolder(itemView)
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(sectionList[i])
    }

    override fun getItemCount(): Int = sectionList.size

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        private val textViewTitle: TextView = itemView.findViewById(R.id.tv_title)

        fun bind(section: UsedeskSection) {
            imageViewIcon.setImageBitmap(null)
            textViewTitle.text = section.title
            setImage(imageViewIcon, section.image)
            itemView.setOnClickListener {
                onSectionClickListener.onSectionClick(section.id)
            }
        }
    }
}