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
        private val viewModel: SectionsViewModel,
        recyclerView: RecyclerView,
        private val onSectionClick: (Long) -> Unit
) : RecyclerView.Adapter<SectionsAdapter.SectionViewHolder>() {

    private var sectionList = listOf<UsedeskSection>()

    init {
        recyclerView.adapter = this
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        val binding = inflateItem(viewGroup,
                R.layout.usedesk_item_section,
                R.style.Usedesk_KnowledgeBase) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }
        return SectionViewHolder(binding)
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(sectionList[i])
    }

    override fun getItemCount(): Int = sectionList.size

    inner class SectionViewHolder(
            private val binding: Binding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(section: UsedeskSection) {
            binding.ivIcon.setImageBitmap(null)
            binding.tvTitle.text = section.title
            setImage(binding.ivIcon, section.thumbnail)
            itemView.setOnClickListener {
                onSectionClick(section.id)
            }
        }
    }

    internal class Binding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val ivIcon: ImageView = rootView.findViewById(R.id.iv_icon)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }
}