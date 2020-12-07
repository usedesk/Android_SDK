package ru.usedesk.chat_gui.chat.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.chat_gui.databinding.UsedeskItemChatAttachedFileBinding
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.setImageCenter

internal class AttachedFilesAdapter(
        private val chatViewModel: ChatViewModel,
        recyclerView: RecyclerView
) : RecyclerView.Adapter<AttachedFilesAdapter.ViewHolder>() {

    private var files: List<UsedeskFileInfo> = listOf()

    init {
        recyclerView.adapter = this
    }

    fun update(attachedFiles: List<UsedeskFileInfo>) {
        files = attachedFiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        return ViewHolder(inflateItem(R.layout.usedesk_item_chat_attached_file, viewGroup))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.bind(files[i])
    }

    override fun getItemCount() = files.size

    inner class ViewHolder(
            private val binding: UsedeskItemChatAttachedFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(usedeskFileInfo: UsedeskFileInfo) {
            setImageCenter(binding.ivPreview, usedeskFileInfo.uri, R.drawable.ic_attached_file)
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
}