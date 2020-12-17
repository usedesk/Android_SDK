package ru.usedesk.chat_gui.chat.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.chat_sdk.entity.UsedeskFileInfo
import ru.usedesk.common_gui.UsedeskBinding
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
        return ViewHolder(inflateItem(viewGroup,
                R.layout.usedesk_item_chat_attached_file) {
            AttachedFileBinding(it)
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

    internal class AttachedFileBinding(rootView: View) : UsedeskBinding(rootView) {
        val ivPreview: ImageView = rootView.findViewById(R.id.iv_preview)
        val ivDetach: ImageView = rootView.findViewById(R.id.iv_detach)
        val tvTitle: TextView = rootView.findViewById(R.id.tv_title)
    }
}