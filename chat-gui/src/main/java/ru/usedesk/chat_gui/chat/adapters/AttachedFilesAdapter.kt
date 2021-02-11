package ru.usedesk.chat_gui.chat.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.*

internal class AttachedFilesAdapter(
        private val chatViewModel: ChatViewModel,
        private val recyclerView: RecyclerView
) : RecyclerView.Adapter<AttachedFilesAdapter.ViewHolder>(), IUsedeskAdapter<ChatViewModel> {

    private var files: List<UsedeskFileInfo> = listOf()

    init {
        recyclerView.adapter = this
    }

    override fun onLiveData(viewModel: ChatViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.fileInfoListLiveData.observe(lifecycleOwner) {
            onItems(it ?: listOf())
        }
    }

    private fun onItems(attachedFiles: List<UsedeskFileInfo>) {
        if (files != attachedFiles) {
            files = attachedFiles
            notifyDataSetChanged()
            recyclerView.visibility = visibleGone(files.isNotEmpty())
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_chat_attached_file,
                R.style.Usedesk_Chat_Attached_File) { rootView, defaultStyleId ->
            AttachedFileBinding(rootView, defaultStyleId)
        })
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.bind(files[i])
    }

    override fun getItemCount() = files.size

    inner class ViewHolder(
            private val binding: AttachedFileBinding
    ) : RecyclerView.ViewHolder(binding.rootView) {

        fun bind(usedeskFileInfo: UsedeskFileInfo) {
            val previewImageId = binding.styleValues
                    .getStyleValues(R.attr.usedesk_chat_attached_file_preview_image)
                    .getId(R.attr.usedesk_drawable_1)

            showImage(binding.ivPreview,
                    previewImageId,
                    usedeskFileInfo.uri.toString(),
                    ignoreCache = true)

            binding.ivDetach.setOnClickListener {
                chatViewModel.detachFile(usedeskFileInfo)
            }
            binding.tvTitle.text = if (!usedeskFileInfo.isImage()) {
                usedeskFileInfo.name
            } else {
                ""
            }
        }
    }

    internal class AttachedFileBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val ivPreview: ImageView = rootView.findViewById(R.id.iv_preview)
        val ivDetach: ImageView = rootView.findViewById(R.id.iv_detach)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }
}