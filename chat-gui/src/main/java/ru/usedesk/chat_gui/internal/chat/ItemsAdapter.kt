package ru.usedesk.chat_gui.internal.chat

import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.databinding.*
import ru.usedesk.chat_sdk.external.entity.*
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import ru.usedesk.common_gui.internal.inflateItem
import ru.usedesk.common_gui.internal.setImage
import ru.usedesk.common_gui.internal.showImage
import ru.usedesk.common_gui.internal.visibleGone
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

internal class ItemsAdapter(
        private val viewModel: ChatViewModel,
        private val recyclerView: RecyclerView,
        private val customAgentName: String?,
        owner: LifecycleOwner,
        private val onFileClick: (UsedeskFile) -> Unit,
        private val onHtmlClick: (String) -> Unit
) : RecyclerView.Adapter<ItemsAdapter.ChatItemViewHolder>() {

    private var items: List<UsedeskChatItem> = listOf()

    init {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
            reverseLayout = true
        }
        recyclerView.adapter = this
        viewModel.chatItemsLiveData.observe(owner) {
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
                MessageTextAgentViewHolder(inflateItem(R.layout.usedesk_item_chat_message_text_agent, parent))
            }
            UsedeskChatItem.Type.TYPE_AGENT_FILE.value -> {
                MessageFileAgentViewHolder(inflateItem(R.layout.usedesk_item_chat_message_file_agent, parent))
            }
            UsedeskChatItem.Type.TYPE_AGENT_IMAGE.value -> {
                MessageImageAgentViewHolder(inflateItem(R.layout.usedesk_item_chat_message_image_agent, parent))
            }
            UsedeskChatItem.Type.TYPE_CLIENT_TEXT.value -> {
                MessageTextClientViewHolder(inflateItem(R.layout.usedesk_item_chat_message_text_client, parent))
            }
            UsedeskChatItem.Type.TYPE_CLIENT_FILE.value -> {
                MessageFileClientViewHolder(inflateItem(R.layout.usedesk_item_chat_message_file_client, parent))
            }
            UsedeskChatItem.Type.TYPE_CLIENT_IMAGE.value -> {
                MessageImageClientViewHolder(inflateItem(R.layout.usedesk_item_chat_message_image_client, parent))
            }
            UsedeskChatItem.Type.TYPE_DATE.value -> {
                DateViewHolder(inflateItem(R.layout.usedesk_item_chat_date, parent))
            }
            else -> {
                throw RuntimeException("Unknown view type:$viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: ChatItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position].type.value

    internal abstract class ChatItemViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(position: Int)
    }

    internal abstract inner class MessageViewHolder(
            itemView: View,
            private val tvTime: TextView
    ) : ChatItemViewHolder(itemView) {

        override fun bind(position: Int) {
            bindTime(position)
        }

        private fun bindTime(position: Int) {
            val chatItem = items[position]
            val formatted = getFormattedTime(chatItem.calendar)
            tvTime.text = formatted
        }

        fun bindAgent(position: Int,
                      tvName: TextView,
                      avatarBinding: UsedeskItemChatAvatarBinding) {
            val messageAgent = items[position] as UsedeskMessageAgent

            tvName.text = customAgentName ?: messageAgent.name
            tvName.visibility = visibleGone(!isSameAgent(messageAgent, position + 1))

            val initials = messageAgent.name.split(' ')
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .map { it[0] }
                    .joinToString(separator = "")
            avatarBinding.tvAvatar.text = initials
            avatarBinding.ivAvatar.setImageResource(if (initials.isEmpty()) {
                R.drawable.background_avatar_def
            } else {
                R.drawable.background_avatar_dark
            })

            setImage(avatarBinding.ivAvatar, messageAgent.avatar, 0)
            avatarBinding.lContainer.visibility = visibleGone(!isSameAgent(messageAgent, position - 1))
        }

        fun bindClient(position: Int,
                       tvName: TextView) {
            tvName.visibility = visibleGone(items.getOrNull(position + 1) !is UsedeskMessageClient)
        }

        private fun isSameAgent(messageAgent: UsedeskMessageAgent, anotherPosition: Int): Boolean {
            val anotherChatItem = items.getOrNull(anotherPosition)
            return anotherChatItem is UsedeskMessageAgent && anotherChatItem.avatar == messageAgent.avatar
        }
    }

    internal abstract inner class MessageTextViewHolder(
            itemView: View,
            private val binding: UsedeskItemChatMessageTextBinding)
        : MessageViewHolder(itemView, binding.tvTime) {

        override fun bind(position: Int) {
            super.bind(position)

            val messageText = items[position] as UsedeskMessageText

            binding.tvText.text = Html.fromHtml(messageText.text)

            binding.tvLink.visibility = visibleGone(messageText.html.isNotEmpty())
            binding.tvLink.setOnClickListener {
                onHtmlClick(messageText.html)
            }
        }
    }

    internal abstract inner class MessageFileViewHolder(
            itemView: View,
            private val binding: UsedeskItemChatMessageFileBinding
    ) : MessageViewHolder(itemView, binding.tvTime) {

        override fun bind(position: Int) {
            super.bind(position)

            val messageFile = items[position] as UsedeskMessageFile

            val name = messageFile.file.name
            binding.tvFileName.text = name
            val index = name.lastIndexOf('.')
            binding.tvExtension.text = if (index >= 0) {
                name.substring(index + 1)
                        .toUpperCase(Locale.getDefault())
            } else {
                ""
            }
            binding.tvFileSize.text = messageFile.file.size
            binding.lRoot.setOnClickListener {
                onFileClick(messageFile.file)
            }
        }
    }

    internal abstract inner class MessageImageViewHolder(
            itemView: View,
            private val binding: UsedeskItemChatMessageImageBinding
    ) : MessageViewHolder(itemView, binding.tvTime) {

        override fun bind(position: Int) {
            super.bind(position)
            bindImage(position)
        }

        private fun bindImage(position: Int) {
            val messageFile = items[position] as UsedeskMessageFile

            binding.ivPreview.setOnClickListener(null)
            binding.ivError.setOnClickListener(null)
            showImage(binding.ivPreview,
                    R.drawable.ic_image_loading,
                    messageFile.file.content,
                    binding.pbLoading,
                    binding.ivError, {
                binding.ivPreview.setOnClickListener {
                    onFileClick(messageFile.file)
                }
            }, {
                binding.ivError.setOnClickListener {
                    bindImage(position)
                }
            })
        }
    }

    internal inner class DateViewHolder(
            private val binding: UsedeskItemChatDateBinding
    ) : ChatItemViewHolder(binding.root) {

        override fun bind(position: Int) {
            val chatItem = items[position]
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
            private val binding: UsedeskItemChatMessageTextClientBinding
    ) : MessageTextViewHolder(binding.root, binding.content) {
        override fun bind(position: Int) {
            super.bind(position)
            bindClient(position, binding.tvName)
        }
    }

    internal inner class MessageFileClientViewHolder(
            private val binding: UsedeskItemChatMessageFileClientBinding
    ) : MessageFileViewHolder(binding.root, binding.content) {
        override fun bind(position: Int) {
            super.bind(position)
            bindClient(position, binding.tvName)
        }
    }

    internal inner class MessageImageClientViewHolder(
            private val binding: UsedeskItemChatMessageImageClientBinding
    ) : MessageImageViewHolder(binding.root, binding.content) {
        override fun bind(position: Int) {
            super.bind(position)
            bindClient(position, binding.tvName)
        }
    }

    internal inner class MessageTextAgentViewHolder(
            private val binding: UsedeskItemChatMessageTextAgentBinding
    ) : MessageTextViewHolder(binding.root, binding.content) {

        override fun bind(position: Int) {
            super.bind(position)
            bindAgent(position, binding.tvName, binding.avatar)
        }
    }

    internal inner class MessageFileAgentViewHolder(
            private val binding: UsedeskItemChatMessageFileAgentBinding
    ) : MessageFileViewHolder(binding.root, binding.content) {

        override fun bind(position: Int) {
            super.bind(position)
            bindAgent(position, binding.tvName, binding.avatar)
        }
    }

    internal inner class MessageImageAgentViewHolder(
            private val binding: UsedeskItemChatMessageImageAgentBinding
    ) : MessageImageViewHolder(binding.root, binding.content) {

        override fun bind(position: Int) {
            super.bind(position)
            bindAgent(position, binding.tvName, binding.avatar)
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