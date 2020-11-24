package ru.usedesk.knowledgebase_gui.internal.screens.pages.sections

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.external.IUsedeskViewCustomizer
import ru.usedesk.common_gui.internal.ImageUtils
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection

class SectionsAdapter internal constructor(private val sectionList: List<UsedeskSection>,
                                           private val onSectionClickListener: IOnSectionClickListener,
                                           private val usedeskViewCustomizer: IUsedeskViewCustomizer) : RecyclerView.Adapter<SectionsAdapter.SectionViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        val view = usedeskViewCustomizer.createView(viewGroup, R.layout.usedesk_item_section, R.style.Usedesk_Theme_KnowledgeBase)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(sectionList[i])
    }

    override fun getItemCount(): Int {
        return sectionList.size
    }

    internal inner class SectionViewHolder(private val rootView: View) : RecyclerView.ViewHolder(rootView) {
        private val imageViewIcon: ImageView
        private val textViewTitle: TextView
        fun bind(section: UsedeskSection) {
            imageViewIcon.setImageBitmap(null)
            textViewTitle.text = section.title
            ImageUtils.setImage(imageViewIcon, section.image)
            rootView.setOnClickListener { v: View? -> onSectionClickListener.onSectionClick(section.id) }
        }

        init {
            imageViewIcon = itemView.findViewById(R.id.iv_icon)
            textViewTitle = itemView.findViewById(R.id.tv_title)
        }
    }
}