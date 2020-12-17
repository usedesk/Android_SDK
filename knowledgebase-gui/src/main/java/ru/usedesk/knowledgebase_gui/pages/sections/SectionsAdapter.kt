package ru.usedesk.knowledgebase_gui.pages.sections

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.setImage
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class SectionsAdapter(
        private val sectionList: List<UsedeskSection>,
        private val onSectionClickListener: IOnSectionClickListener
) : RecyclerView.Adapter<SectionsAdapter.SectionViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        return SectionViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_section) {
            SectionBinding(it)
        })
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(sectionList[i])
    }

    override fun getItemCount(): Int = sectionList.size

    inner class SectionViewHolder(
            private val binding: SectionBinding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(section: UsedeskSection) {
            binding.ivIcon.setImageBitmap(null)
            binding.tvTitle.text = section.title
            setImage(binding.ivIcon, section.image)
            itemView.setOnClickListener {
                onSectionClickListener.onSectionClick(section.id)
            }
        }
    }

    internal class SectionBinding(rootView: View) : UsedeskBinding(rootView) {
        val ivIcon: ImageView = rootView.findViewById(R.id.iv_icon)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }
}