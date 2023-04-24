
package ru.usedesk.chat_gui.chat.messages.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.UsedeskBinding
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.onEachWithOld
import ru.usedesk.common_gui.showImage
import ru.usedesk.common_gui.visibleGone

internal class AttachedFilesAdapter(
    private val recyclerView: RecyclerView,
    private val viewModel: MessagesViewModel,
    lifecycleCoroutineScope: LifecycleCoroutineScope,
) : RecyclerView.Adapter<AttachedFilesAdapter.ViewHolder>() {

    private var files = listOf<UsedeskFileInfo>()

    init {
        recyclerView.adapter = this
        viewModel.modelFlow.onEachWithOld(lifecycleCoroutineScope) { old, new ->
            if (old?.messageDraft?.files != new.messageDraft.files) {
                val oldFiles = files
                files = new.messageDraft.files

                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize() = oldFiles.size

                    override fun getNewListSize() = files.size

                    override fun areItemsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldFile = oldFiles[oldItemPosition]
                        val newFile = files[newItemPosition]
                        return oldFile.uri == newFile.uri
                    }

                    override fun areContentsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        val oldFile = oldFiles[oldItemPosition]
                        val newFile = files[newItemPosition]
                        return oldFile.uri == newFile.uri
                    }
                }).dispatchUpdatesTo(this)

                recyclerView.visibility = visibleGone(files.isNotEmpty())
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int) = ViewHolder(
        inflateItem(
            viewGroup,
            R.layout.usedesk_item_chat_attached_file,
            R.style.Usedesk_Chat_Attached_File,
            ::AttachedFileBinding
        )
    )

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) = viewHolder.bind(files[i])

    override fun getItemCount() = files.size

    inner class ViewHolder(
        private val binding: AttachedFileBinding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(usedeskFileInfo: UsedeskFileInfo) {
            val previewImageId = binding.styleValues
                .getStyleValues(R.attr.usedesk_chat_attached_file_preview_image)
                .getId(R.attr.usedesk_drawable_1)

            binding.ivPreview.showImage(
                usedeskFileInfo.uri.toString(),
                previewImageId,
                ignoreCache = true
            )

            binding.ivDetach.setOnClickListener {
                viewModel.onEvent(MessagesViewModel.Event.DetachFile(usedeskFileInfo))
            }
            binding.tvTitle.text = when {
                usedeskFileInfo.isImage() || usedeskFileInfo.isVideo() -> ""
                else -> usedeskFileInfo.name
            }
        }
    }

    internal class AttachedFileBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val ivPreview: ImageView = rootView.findViewById(R.id.iv_preview)
        val ivDetach: ImageView = rootView.findViewById(R.id.iv_detach)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }
}