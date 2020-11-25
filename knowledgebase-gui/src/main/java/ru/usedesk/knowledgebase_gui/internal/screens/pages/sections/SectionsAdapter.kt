package ru.usedesk.knowledgebase_gui.internal.screens.pages.sections

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.internal.inflateItem
import ru.usedesk.common_gui.internal.setImage
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_gui.databinding.UsedeskItemSectionBinding
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection

class SectionsAdapter(
        private val sectionList: List<UsedeskSection>,
        private val onSectionClickListener: IOnSectionClickListener
) : RecyclerView.Adapter<SectionsAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        return SectionViewHolder(inflateItem(R.layout.usedesk_item_section, viewGroup))
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(sectionList[i])
    }

    override fun getItemCount(): Int = sectionList.size

    inner class SectionViewHolder(
            private val binding: UsedeskItemSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(section: UsedeskSection) {
            binding.ivIcon.setImageBitmap(null)
            binding.tvTitle.text = section.title
            setImage(binding.ivIcon, section.image)
            itemView.setOnClickListener {
                onSectionClickListener.onSectionClick(section.id)
            }
        }
    }
}