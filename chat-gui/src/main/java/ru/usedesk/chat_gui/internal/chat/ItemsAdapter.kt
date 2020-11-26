package ru.usedesk.chat_gui.internal.chat

import android.graphics.Color
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.*
import ru.usedesk.chat_sdk.external.entity.ticketitem.*
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
    private var items: List<ChatItem> = listOf()

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
            ChatItem.Type.TYPE_AGENT_TEXT.value -> {
                MessageTextOtherViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_text_agent, parent))
            }
            ChatItem.Type.TYPE_AGENT_FILE.value -> {
                MessageFileOtherViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_file_agent, parent))
            }
            ChatItem.Type.TYPE_AGENT_IMAGE.value -> {
                MessageImageOtherViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_image_agent, parent))
            }
            ChatItem.Type.TYPE_CLIENT_TEXT.value -> {
                MessageTextYourViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_text_client, parent))
            }
            ChatItem.Type.TYPE_CLIENT_FILE.value -> {
                MessageFileYourViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_file_client, parent))
            }
            ChatItem.Type.TYPE_CLIENT_IMAGE.value -> {
                MessageImageYourViewHolder(inflateItem(R.layout.usedesk_item_ticket_comment_image_client, parent))
            }
            ChatItem.Type.TYPE_DATE.value -> {
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

        abstract fun bind(chatItem: ChatItem, position: Int)
    }

    internal abstract inner class MessageViewHolder(
            itemView: View,
            private val tvTime: TextView
    ) : ChatItemViewHolder(itemView) {

        override fun bind(chatItem: ChatItem, position: Int) {
            bindTime(chatItem)
        }

        private fun bindTime(chatItem: ChatItem) {
            val formatted = getFormattedTime(chatItem.calendar)
            tvTime.text = formatted
        }

        fun bindAgent(bubble: View,
                      tvName: TextView,
                      tvAvatar: TextView,
                      tvText: TextView?,
                      timeBinding: UsedeskItemTicketCommentTimeBinding,
                      dark: Boolean,
                      messageAgent: MessageAgent) {
            bubble.setBackgroundResource(R.drawable.bubble_other)
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
            bindTime(dark, timeBinding)
        }

        fun bindClient(bubble: View,
                       ivReceived: ImageView,
                       tvText: TextView?,
                       timeBinding: UsedeskItemTicketCommentTimeBinding,
                       dark: Boolean,
                       messageClient: MessageClient) {
            bubble.setBackgroundResource(R.drawable.bubble_your)
            tvText?.setTextColor(colorBlack)

            val receivedId = when (messageClient.received) {
                true -> {
                    if (dark) {
                        R.drawable.ic_sended_light
                    } else {
                        R.drawable.ic_sended_dark
                    }
                }
                false -> {
                    if (dark) {
                        R.drawable.ic_received_light
                    } else {
                        R.drawable.ic_received_dark
                    }
                }
                else -> {
                    0
                }
            }
            ivReceived.setImageResource(receivedId)
            bindTime(dark, timeBinding)
        }

        private fun bindTime(dark: Boolean, timeBinding: UsedeskItemTicketCommentTimeBinding) {
            val backgroundId = if (dark) {
                R.drawable.time_background_dark
            } else {
                R.drawable.time_background_light
            }
            timeBinding.lRoot.setBackgroundResource(backgroundId)
            val textColorId = Color.parseColor(if (dark) {
                "#CCFFFFFF"
            } else {
                "#828282"
            })
            timeBinding.tvTime.setTextColor(textColorId)
        }
    }

    internal abstract inner class MessageTextViewHolder(
            itemView: View,
            private val binding: UsedeskItemTicketCommentTextBinding)
        : MessageViewHolder(itemView, binding.time.tvTime) {

        override fun bind(chatItem: ChatItem, position: Int) {
            super.bind(chatItem, position)
            bindText(chatItem as MessageText)
        }

        private fun bindText(messageText: MessageText) {
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
    ) : MessageViewHolder(itemView, binding.time.tvTime) {

        override fun bind(chatItem: ChatItem,
                          position: Int) {
            super.bind(chatItem, position)
            bindFile(chatItem as MessageFile)
        }

        private fun bindFile(messageFile: MessageFile) {
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
    ) : MessageViewHolder(itemView, binding.time.tvTime) {

        override fun bind(chatItem: ChatItem, position: Int) {
            super.bind(chatItem, position)
            bindImage(chatItem as MessageFile)
        }

        private fun bindImage(messageFile: MessageFile) {
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

        override fun bind(chatItem: ChatItem, position: Int) {
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

    internal inner class MessageTextYourViewHolder(
            private val binding: UsedeskItemTicketCommentTextClientBinding
    ) : MessageTextViewHolder(binding.root, binding.content) {

        override fun bind(chatItem: ChatItem, position: Int) {
            super.bind(chatItem, position)
            bindClient(binding.lYour,
                    binding.content.time.ivReceived,
                    binding.content.tvText,
                    binding.content.time,
                    false,
                    chatItem as MessageClient)
        }
    }

    internal inner class MessageFileYourViewHolder(
            private val binding: UsedeskItemTicketCommentFileClientBinding
    ) : MessageFileViewHolder(binding.root, binding.content) {

        override fun bind(chatItem: ChatItem, position: Int) {
            super.bind(chatItem, position)
            bindClient(binding.lYour,
                    binding.content.time.ivReceived,
                    binding.content.tvFileName,
                    binding.content.time,
                    false,
                    chatItem as MessageClient)
            binding.content.ivFileType.setImageResource(R.drawable.ic_file_light)
            binding.content.tvExtension.setTextColor(Color.parseColor("#242B33"))
        }
    }

    internal inner class MessageImageYourViewHolder(
            private val binding: UsedeskItemTicketCommentImageClientBinding
    ) : MessageImageViewHolder(binding.root, binding.content) {

        override fun bind(chatItem: ChatItem, position: Int) {
            super.bind(chatItem, position)
            bindClient(binding.lYour,
                    binding.content.time.ivReceived,
                    null,
                    binding.content.time,
                    true,
                    chatItem as MessageClient)
        }
    }

    internal inner class MessageTextOtherViewHolder(
            private val binding: UsedeskItemTicketCommentTextAgentBinding
    ) : MessageTextViewHolder(binding.root, binding.content) {

        init {
            binding.content.time.ivReceived.visibility = View.GONE
        }

        override fun bind(chatItem: ChatItem, position: Int) {
            super.bind(chatItem, position)
            bindAgent(binding.lOther,
                    binding.tvName,
                    binding.tvAvatar,
                    binding.content.tvText,
                    binding.content.time,
                    false,
                    chatItem as MessageAgent)
        }
    }

    internal inner class MessageFileOtherViewHolder(
            private val binding: UsedeskItemTicketCommentFileAgentBinding
    ) : MessageFileViewHolder(binding.root, binding.content) {

        init {
            binding.content.time.ivReceived.visibility = View.GONE
        }

        override fun bind(chatItem: ChatItem, position: Int) {
            super.bind(chatItem, position)
            bindAgent(binding.lOther,
                    binding.tvName,
                    binding.tvAvatar,
                    binding.content.tvFileName,
                    binding.content.time,
                    false,
                    chatItem as MessageAgent)
            binding.content.ivFileType.setImageResource(R.drawable.ic_file_dark)
            binding.content.tvExtension.setTextColor(Color.parseColor("#F4F6FA"))
        }
    }

    internal inner class MessageImageOtherViewHolder(
            private val binding: UsedeskItemTicketCommentImageAgentBinding
    ) : MessageImageViewHolder(binding.root, binding.content) {

        init {
            binding.content.time.ivReceived.visibility = View.GONE
        }

        override fun bind(chatItem: ChatItem, position: Int) {
            super.bind(chatItem, position)
            bindAgent(binding.lOther,
                    binding.tvName,
                    binding.tvAvatar,
                    null,
                    binding.content.time,
                    true,
                    chatItem as MessageAgent)
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