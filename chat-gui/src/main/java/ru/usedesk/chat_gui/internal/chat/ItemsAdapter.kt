package ru.usedesk.chat_gui.internal.chat

import android.graphics.Color
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.*
import ru.usedesk.chat_sdk.external.entity.chat.UsedeskChatItem
import ru.usedesk.chat_sdk.external.entity.chat.UsedeskMessageAgent
import ru.usedesk.chat_sdk.external.entity.chat.UsedeskMessageFile
import ru.usedesk.chat_sdk.external.entity.chat.UsedeskMessageText
import ru.usedesk.common_gui.internal.formatSize
import ru.usedesk.common_gui.internal.inflateItem
import ru.usedesk.common_gui.internal.showImage
import ru.usedesk.common_gui.internal.visibleGone
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

internal class ItemsAdapter(
        private val viewModel: ChatViewModel,
        private val recyclerView: RecyclerView,
        private val customAgentName: String?,
        owner: LifecycleOwner
) : RecyclerView.Adapter<ItemsAdapter.ChatItemViewHolder>() {

    private val colorBlack: Int = recyclerView.resources.getColor(R.color.usedesk_black)
    private var items: List<UsedeskChatItem> = listOf()

    init {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
            reverseLayout = true
        }
        recyclerView.adapter = this
        viewModel.ticketItemsLiveData.observe(owner) {
            this.items = it ?: listOf()
            notifyDataSetChanged()
        }
    }

    private fun getFormattedTime(calendar: Calendar): String {
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatItemViewHolder {
        return when (viewType) {
            UsedeskChatItem.Type.TYPE_AGENT_TEXT.value -> {
                MessageTextAgentViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_text_agent, parent))
            }
            UsedeskChatItem.Type.TYPE_AGENT_FILE.value -> {
                MessageFileAgentViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_file_agent, parent))
            }
            UsedeskChatItem.Type.TYPE_AGENT_IMAGE.value -> {
                MessageImageAgentViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_image_agent, parent))
            }
            UsedeskChatItem.Type.TYPE_CLIENT_TEXT.value -> {
                MessageTextClientViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_text_client, parent))
            }
            UsedeskChatItem.Type.TYPE_CLIENT_FILE.value -> {
                MessageFileClientViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_file_client, parent))
            }
            UsedeskChatItem.Type.TYPE_CLIENT_IMAGE.value -> {
                MessageImageClientViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_image_client, parent))
            }
            UsedeskChatItem.Type.TYPE_DATE.value -> {
                DateViewHolder(inflateItem(R.layout.usedesk_item_ticket_date, parent))
            }
            else -> {
                throw RuntimeException("Unknown view type:$viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position].type.value

    internal abstract class ChatItemViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(chatItem: UsedeskChatItem, position: Int)
    }

    internal abstract inner class MessageViewHolder(
            itemView: View,
            private val tvTime: TextView
    ) : ChatItemViewHolder(itemView) {

        override fun bind(chatItem: UsedeskChatItem, position: Int) {
            bindTime(chatItem)
        }

        private fun bindTime(chatItem: UsedeskChatItem) {
            val formatted = getFormattedTime(chatItem.calendar)
            tvTime.text = formatted
        }

        fun bindAgent(bubble: View,
                      tvName: TextView,
                      tvAvatar: TextView,
                      tvText: TextView?,
                      messageAgent: UsedeskMessageAgent) {
            bubble.setBackgroundResource(R.drawable.bubble_agent)
            tvText?.setTextColor(colorBlack)

            tvName.text = messageAgent.name
            val initials = messageAgent.name.split(' ')
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .map { it[0] }
                    .joinToString(separator = "")
            tvAvatar.text = initials//TODO: avatar image
            tvAvatar.setBackgroundResource(if (initials.isEmpty()) {
                R.drawable.background_agent_avatar_def
            } else {
                R.drawable.background_agent_avatar_black
            })
        }
    }

    internal abstract inner class MessageTextViewHolder(
            itemView: View,
            private val binding: UsedeskItemTicketCommentTextBinding)
        : MessageViewHolder(itemView, binding.tvTime) {

        override fun bind(chatItem: UsedeskChatItem, position: Int) {
            super.bind(chatItem, position)
            bindText(chatItem as UsedeskMessageText)
        }

        private fun bindText(messageText: UsedeskMessageText) {
            val text: String
            val html: String


            val divIndex = messageText.text.indexOf("<div")

            if (divIndex >= 0) {
                text = messageText.text.substring(0, divIndex)

                html = messageText.text
                        .removePrefix(text)
            } else {
                text = messageText.text
                html = ""
            }

            binding.tvText.visibility = visibleGone(text.isNotEmpty())
            val convertedText = text
                    .replace("<strong data-verified=\"redactor\" data-redactor-tag=\"strong\">", "<b>")
                    .replace("</strong>", "</b>")
                    .replace("<em data-verified=\"redactor\" data-redactor-tag=\"em\">", "<i>")
                    .replace("</em>", "</i>")
                    .replace("</p>", "")
                    .removePrefix("<p>")
                    .trim()

            binding.tvText.text = Html.fromHtml(convertedText.trim())

            binding.tvLink.visibility = visibleGone(html.isNotEmpty())
            binding.tvLink.setOnClickListener {
                viewModel.onShowHtmlClick(html)
            }
        }
    }

    internal abstract inner class MessageFileViewHolder(
            itemView: View,
            private val binding: UsedeskItemTicketCommentFileBinding
    ) : MessageViewHolder(itemView, binding.tvTime) {

        override fun bind(chatItem: UsedeskChatItem,
                          position: Int) {
            super.bind(chatItem, position)
            bindFile(chatItem as UsedeskMessageFile)
        }

        private fun bindFile(messageFile: UsedeskMessageFile) {
            val name = messageFile.file.name
            binding.tvFileName.text = name
            val index = name.lastIndexOf('.')
            binding.tvExtension.text = if (index >= 0) {
                name.substring(index + 1)
                        .toUpperCase(Locale.getDefault())
            } else {
                ""
            }
            val size = messageFile.file.size.toLongOrNull() ?: 0L
            binding.tvFileSize.text = formatSize(recyclerView.context, size)
            binding.lRoot.setOnClickListener {
                viewModel.onClickFile(messageFile)
            }
        }
    }

    internal abstract inner class MessageImageViewHolder(
            itemView: View,
            private val binding: UsedeskItemTicketCommentImageBinding
    ) : MessageViewHolder(itemView, binding.tvTime) {

        override fun bind(chatItem: UsedeskChatItem, position: Int) {
            super.bind(chatItem, position)
            bindImage(chatItem as UsedeskMessageFile)
        }

        private fun bindImage(messageFile: UsedeskMessageFile) {
            binding.ivPreview.setOnClickListener(null)
            binding.ivError.setOnClickListener(null)
            showImage(binding.ivPreview,
                    R.drawable.ic_image_loading,
                    messageFile.file.content,
                    binding.pbLoading,
                    binding.ivError, {
                binding.ivPreview.setOnClickListener {
                    viewModel.onClickFile(messageFile)
                }
            }, {
                binding.ivError.setOnClickListener {
                    bindImage(messageFile)
                }
            })
        }
    }

    internal inner class DateViewHolder(
            private val binding: UsedeskItemTicketDateBinding
    ) : ChatItemViewHolder(binding.root) {

        override fun bind(chatItem: UsedeskChatItem, position: Int) {
            when {
                isToday(chatItem.calendar) -> {
                    binding.tvDate.setText(R.string.today)
                }
                isYesterday(chatItem.calendar) -> {
                    binding.tvDate.setText(R.string.yesterday)
                }
                else -> {
                    val dateFormat: DateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
                    val formatted = dateFormat.format(chatItem.calendar.time)
                    binding.tvDate.text = formatted
                }
            }
        }
    }

    internal inner class MessageTextClientViewHolder(
            binding: UsedeskItemTicketCommentTextClientBinding
    ) : MessageTextViewHolder(binding.root, binding.content)

    internal inner class MessageFileClientViewHolder(
            private val binding: UsedeskItemTicketCommentFileClientBinding
    ) : MessageFileViewHolder(binding.root, binding.content) {

        override fun bind(chatItem: UsedeskChatItem, position: Int) {
            super.bind(chatItem, position)

            binding.content.ivFileType.setImageResource(R.drawable.ic_file_light)
            binding.content.tvExtension.setTextColor(Color.parseColor("#242B33"))
        }
    }

    internal inner class MessageImageClientViewHolder(
            binding: UsedeskItemTicketCommentImageClientBinding
    ) : MessageImageViewHolder(binding.root, binding.content)

    internal inner class MessageTextAgentViewHolder(
            private val binding: UsedeskItemTicketCommentTextAgentBinding
    ) : MessageTextViewHolder(binding.root, binding.content) {

        override fun bind(chatItem: UsedeskChatItem, position: Int) {
            super.bind(chatItem, position)
            bindAgent(binding.lAgent,
                    binding.tvName,
                    binding.tvAvatar,
                    binding.content.tvText,
                    chatItem as UsedeskMessageAgent)
        }
    }

    internal inner class MessageFileAgentViewHolder(
            private val binding: UsedeskItemTicketCommentFileAgentBinding
    ) : MessageFileViewHolder(binding.root, binding.content) {

        override fun bind(chatItem: UsedeskChatItem, position: Int) {
            super.bind(chatItem, position)
            bindAgent(binding.lAgent,
                    binding.tvName,
                    binding.tvAvatar,
                    binding.content.tvFileName,
                    chatItem as UsedeskMessageAgent)
            binding.content.ivFileType.setImageResource(R.drawable.ic_file_dark)
            binding.content.tvExtension.setTextColor(Color.parseColor("#F4F6FA"))
        }
    }

    internal inner class MessageImageAgentViewHolder(
            private val binding: UsedeskItemTicketCommentImageAgentBinding
    ) : MessageImageViewHolder(binding.root, binding.content) {

        override fun bind(chatItem: UsedeskChatItem, position: Int) {
            super.bind(chatItem, position)
            bindAgent(binding.lAgent,
                    binding.tvName,
                    binding.tvAvatar,
                    null,
                    chatItem as UsedeskMessageAgent)
        }
    }

    companion object {
        private fun isToday(calendar: Calendar): Boolean {
            val today = Calendar.getInstance()
            return (today[Calendar.YEAR] == calendar[Calendar.YEAR]
                    && today[Calendar.DAY_OF_YEAR] == calendar[Calendar.DAY_OF_YEAR])
        }

        private fun isYesterday(calendar: Calendar): Boolean {
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            return (yesterday[Calendar.YEAR] == calendar[Calendar.YEAR]
                    && yesterday[Calendar.DAY_OF_YEAR] == calendar[Calendar.DAY_OF_YEAR])
        }
    }
}