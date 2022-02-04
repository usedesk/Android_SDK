package ru.usedesk.knowledgebase_gui.screens.sections

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.showImage
import ru.usedesk.knowledgebase_gui.R
import ru.usedesk.knowledgebase_sdk.entity.UsedeskSection

internal class SectionsAdapter(
    recyclerView: RecyclerView,
    viewModel: SectionsViewModel,
    lifecycleOwner: LifecycleOwner,
    private val onSectionClick: (Long, String) -> Unit
) : RecyclerView.Adapter<SectionsAdapter.SectionViewHolder>() {

    private var sections = listOf<UsedeskSection>()

    init {
        recyclerView.adapter = this
        viewModel.modelLiveData.initAndObserveWithOld(lifecycleOwner) { old, new ->
            if (old?.sections != new.sections) {
                val oldItems = sections
                val newItems = new.sections
                sections = newItems

                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize() = oldItems.size

                    override fun getNewListSize() = newItems.size

                    override fun areItemsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems[newItemPosition]
                        return oldItem.id == newItem.id
                    }

                    override fun areContentsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldItem = oldItems[oldItemPosition]
                        val newItem = newItems[newItemPosition]
                        return oldItem.title == newItem.title &&
                                oldItem.thumbnail == newItem.thumbnail
                    }
                }).dispatchUpdatesTo(this)
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SectionViewHolder {
        val binding = inflateItem(
            viewGroup,
            R.layout.usedesk_item_section,
            R.style.Usedesk_KnowledgeBase_Sections_Page_Section
        ) { rootView, defaultStyleId ->
            Binding(rootView, defaultStyleId)
        }
        return SectionViewHolder(binding)
    }

    override fun onBindViewHolder(sectionViewHolder: SectionViewHolder, i: Int) {
        sectionViewHolder.bind(sections[i])
    }

    override fun getItemCount(): Int = sections.size

    inner class SectionViewHolder(
        private val binding: Binding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        private val noThumbnailId = binding.styleValues
            .getStyleValues(R.attr.usedesk_knowledgebase_sections_page_section_thumbnail_image)
            .getId(R.attr.usedesk_drawable_1)

        fun bind(section: UsedeskSection) {
            binding.ivIcon.setImageBitmap(null)
            binding.tvTitle.text = section.title
            binding.ivIcon.setImageResource(noThumbnailId)
            binding.tvInitials.text = section.title
            section.thumbnail?.also {
                showImage(binding.ivIcon,
                    noThumbnailId,
                    it,
                    onSuccess = {
                        binding.tvInitials.text = ""
                    }
                )
            }
            binding.lClickable.setOnClickListener {
                onSectionClick(section.id, section.title)
            }
        }
    }

    internal class Binding(
        rootView: View,
        defaultStyleId: Int
    ) : UsedeskBinding(rootView, defaultStyleId) {
        val lClickable: ViewGroup = rootView.findViewById(R.id.l_clickable)
        val ivIcon: ImageView = rootView.findViewById(R.id.iv_icon)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
        val tvInitials: TextView = rootView.findViewById(R.id.tv_initials)
    }
}