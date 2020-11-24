package ru.usedesk.chat_gui.internal.chat

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.UsedeskItemChatAttachedFileBinding
import ru.usedesk.chat_sdk.external.entity.UsedeskFileInfo
import ru.usedesk.common_gui.internal.inflateItem
import ru.usedesk.common_gui.internal.setImageCenter

class AttachedFilesAdapter(
        private val chatViewModel: ChatViewModel,
        recyclerView: RecyclerView
) : RecyclerView.Adapter<AttachedFilesAdapter.ViewHolder>() {

    private var files: List<UsedeskFileInfo> = listOf()

    init {
        recyclerView.adapter = this
    }

    fun update(attachedFiles: List<UsedeskFileInfo>) {
        if (files !== attachedFiles) {
            files = attachedFiles
            notifyDataSetChanged()
        }
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
            setImageCenter(binding.ivPreview, usedeskFileInfo.uri, R.drawable.ic_document_black)
            binding.ivDetach.setOnClickListener {
                chatViewModel.detachFile(usedeskFileInfo)
            }
        }
    }
}