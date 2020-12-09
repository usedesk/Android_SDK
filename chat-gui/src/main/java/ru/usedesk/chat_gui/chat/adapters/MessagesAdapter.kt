package ru.usedesk.chat_gui.chat.adapters

import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.chat_gui.databinding.*
import ru.usedesk.chat_sdk.data._entity.UsedeskFile
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.inflateItem
import ru.usedesk.common_gui.setImage
import ru.usedesk.common_gui.showImage
import ru.usedesk.common_gui.visibleGone
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


internal class MessagesAdapter(
        private val viewModel: ChatViewModel,
        private val recyclerView: RecyclerView,
        private val customAgentName: String?,
        owner: LifecycleOwner,
        private val onFileClick: (UsedeskFile) -> Unit,
        private val onHtmlClick: (String) -> Unit,
        private val onUrlClick: (String) -> Unit
) : RecyclerView.Adapter<MessagesAdapter.BaseViewHolder>() {

    private var items: List<UsedeskMessage> = listOf()

    init {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context).apply {
            reverseLayout = true
        }
        recyclerView.adapter = this
        viewModel.messagesLiveData.observe(owner) {
            it.forEachIndexed { index, message ->
                if (message != this.items.getOrNull(index)) {
                    notifyItemChanged(index)
                }
            }
            this.items = it
        }
        viewModel.messageUpdateLiveData.observe(owner) { messageUpdate ->
            items = items.mapIndexed { index, message ->
                if (messageUpdate.id == message.id) {
                    if (message is UsedeskMessageClient) {
                        notifyItemChanged(index)
                    }
                    messageUpdate
                } else {
                    message
                }
            }
        }
    }

    private fun getFormattedTime(calendar: Calendar): String {
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            UsedeskMessage.Type.TYPE_AGENT_TEXT.value -> {
                MessageTextAgentViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_text_agent,
                        R.style.Usedesk_Chat_Message_Agent_Text))
            }
            UsedeskMessage.Type.TYPE_AGENT_FILE.value -> {
                MessageFileAgentViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_file_agent,
                        R.style.Usedesk_Chat_Message_Agent_File))
            }
            UsedeskMessage.Type.TYPE_AGENT_IMAGE.value -> {
                MessageImageAgentViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_image_agent,
                        R.style.Usedesk_Chat_Message_Agent_Image))
            }
            UsedeskMessage.Type.TYPE_CLIENT_TEXT.value -> {
                MessageTextClientViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_text_client,
                        R.style.Usedesk_Chat_Message_Client_Text))
            }
            UsedeskMessage.Type.TYPE_CLIENT_FILE.value -> {
                MessageFileClientViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_file_client,
                        R.style.Usedesk_Chat_Message_Client_File))
            }
            UsedeskMessage.Type.TYPE_CLIENT_IMAGE.value -> {
                MessageImageClientViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_image_client,
                        R.style.Usedesk_Chat_Message_Client_Image))
            }
            else -> {
                throw RuntimeException("Unknown view type:$viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = items[position].type.value

    internal abstract class BaseViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(position: Int)
    }

    internal abstract inner class MessageViewHolder(
            itemView: View,
            private val bindingDate: UsedeskViewChatDateBinding,
            private val tvTime: TextView
    ) : BaseViewHolder(itemView) {

        override fun bind(position: Int) {
            val message = items[position]
            val formatted = getFormattedTime(message.calendar)
            tvTime.text = formatted

            val previousMessage = items.getOrNull(position + 1)
            if (isSameDay(previousMessage?.calendar, message.calendar)) {
                bindingDate.root.visibility = View.GONE
            } else {
                bindingDate.root.visibility = View.VISIBLE
                when {
                    isToday(message.calendar) -> {
                        bindingDate.tvDate.setText(R.string.today)
                    }
                    isYesterday(message.calendar) -> {
                        bindingDate.tvDate.setText(R.string.yesterday)
                    }
                    else -> {
                        val dateFormat: DateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
                        val formatted = dateFormat.format(message.calendar.time)
                        bindingDate.tvDate.text = formatted
                    }
                }
            }
        }

        private fun isSameDay(calendarA: Calendar?, calendarB: Calendar): Boolean {
            return calendarA?.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR) &&
                    calendarA.get(Calendar.DAY_OF_YEAR) == calendarB.get(Calendar.DAY_OF_YEAR)
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
            tvName.visibility = visibleGone(items.getOrNull(position + 1) is UsedeskMessageAgent)
        }

        private fun isSameAgent(messageAgent: UsedeskMessageAgent, anotherPosition: Int): Boolean {
            val anotherMessage = items.getOrNull(anotherPosition)
            return anotherMessage is UsedeskMessageAgent
                    && anotherMessage.avatar == messageAgent.avatar
                    && anotherMessage.name == messageAgent.name
        }
    }

    internal abstract inner class MessageTextViewHolder(
            itemView: View,
            private val binding: UsedeskItemChatMessageTextBinding,
            bindingDate: UsedeskViewChatDateBinding)
        : MessageViewHolder(itemView, bindingDate, binding.tvTime) {

        override fun bind(position: Int) {
            super.bind(position)

            binding.lFeedback.visibility = View.GONE

            val messageText = items[position] as UsedeskMessageText

            binding.tvText.text = Html.fromHtml(messageText.text)
            binding.tvText.visibility = View.VISIBLE

            binding.tvLink.visibility = visibleGone(messageText.html.isNotEmpty())
            binding.tvLink.setOnClickListener {
                onHtmlClick(messageText.html)
            }
        }
    }

    internal abstract inner class MessageFileViewHolder(
            itemView: View,
            private val binding: UsedeskItemChatMessageFileBinding,
            bindingDate: UsedeskViewChatDateBinding
    ) : MessageViewHolder(itemView, bindingDate, binding.tvTime) {

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
            private val binding: UsedeskItemChatMessageImageBinding,
            bindingDate: UsedeskViewChatDateBinding
    ) : MessageViewHolder(itemView, bindingDate, binding.tvTime) {

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

    internal inner class MessageTextClientViewHolder(
            private val binding: UsedeskItemChatMessageTextClientBinding
    ) : MessageTextViewHolder(binding.root, binding.content, binding.date) {
        override fun bind(position: Int) {
            super.bind(position)
            bindClient(position, binding.tvName)
        }
    }

    internal inner class MessageFileClientViewHolder(
            private val binding: UsedeskItemChatMessageFileClientBinding
    ) : MessageFileViewHolder(binding.root, binding.content, binding.date) {
        override fun bind(position: Int) {
            super.bind(position)
            bindClient(position, binding.tvName)
        }
    }

    internal inner class MessageImageClientViewHolder(
            private val binding: UsedeskItemChatMessageImageClientBinding
    ) : MessageImageViewHolder(binding.root, binding.content, binding.date) {
        override fun bind(position: Int) {
            super.bind(position)
            bindClient(position, binding.tvName)
        }
    }

    internal inner class MessageTextAgentViewHolder(
            private val binding: UsedeskItemChatMessageTextAgentBinding
    ) : MessageTextViewHolder(binding.root, binding.content, binding.date) {

        private val buttonsAdapter = ButtonsAdapter(binding.content.rvButtons) {
            if (it.url.isNotEmpty()) {
                onUrlClick(it.url)
            } else {
                viewModel.onSend(it.text)
            }
        }

        override fun bind(position: Int) {
            super.bind(position)
            bindAgent(position, binding.tvName, binding.avatar)

            val messageAgentText = items[position] as UsedeskMessageAgentText
            buttonsAdapter.update(messageAgentText.buttons)

            val ivLike = binding.content.ivLike
            val ivDislike = binding.content.ivDislike

            when {
                messageAgentText.feedback != null -> {
                    binding.content.lFeedback.visibility = View.VISIBLE

                    aloneSmile(ivLike,
                            binding.content.lFeedback,
                            R.drawable.ic_smile_happy_colored,
                            messageAgentText.feedback == UsedeskFeedback.LIKE
                    )

                    aloneSmile(ivDislike,
                            binding.content.lFeedback,
                            R.drawable.ic_smile_sad_colored,
                            messageAgentText.feedback == UsedeskFeedback.DISLIKE
                    )

                    binding.content.tvText.setText(R.string.feedback_thank_you)
                }
                messageAgentText.feedbackNeeded -> {
                    binding.content.lFeedback.visibility = View.VISIBLE

                    enableSmile(ivLike,
                            ivDislike,
                            binding.content.lFeedback,
                            false,
                            R.drawable.ic_smile_happy,
                            R.drawable.ic_smile_happy_colored) {
                        viewModel.sendFeedback(messageAgentText, UsedeskFeedback.LIKE)
                        binding.content.tvText.setText(R.string.feedback_thank_you)
                    }

                    enableSmile(ivDislike,
                            ivLike,
                            binding.content.lFeedback,
                            true,
                            R.drawable.ic_smile_sad,
                            R.drawable.ic_smile_sad_colored) {
                        viewModel.sendFeedback(messageAgentText, UsedeskFeedback.DISLIKE)
                        binding.content.tvText.setText(R.string.feedback_thank_you)
                    }
                }
                else -> {
                    binding.content.lFeedback.visibility = View.GONE
                }
            }
        }

        private fun aloneSmile(imageView: ImageView,
                               container: ViewGroup,
                               imageId: Int,
                               visible: Boolean) {
            imageView.apply {
                setImageResource(imageId)
                post {
                    x = container.width / 4.0f
                }
                alpha = if (visible) {
                    1.0f
                } else {
                    0.0f
                }
                isEnabled = false
            }
        }

        private fun enableSmile(imageViewMain: ImageView,
                                imageViewSub: ImageView,
                                container: ViewGroup,
                                initStart: Boolean,
                                initImageId: Int,
                                activeImageId: Int,
                                onClick: () -> Unit) {
            imageViewMain.apply {
                post {
                    x = if (initStart) {
                        0.0f
                    } else {
                        container.width / 2.0f
                    }
                }
                alpha = 1.0f
                scaleX = 1.0f
                scaleY = 1.0f
                isEnabled = true
                setImageResource(initImageId)
                setOnClickListener {
                    isEnabled = false

                    setImageResource(activeImageId)

                    onClick()

                    animate().setDuration(500)
                            .x(container.width / 4.0f)

                    imageViewSub.animate()
                            .setDuration(500)
                            .alpha(0.0f)
                            .scaleX(0.5f)
                            .scaleY(0.5f)
                }
            }
        }
    }

    internal inner class MessageFileAgentViewHolder(
            private val binding: UsedeskItemChatMessageFileAgentBinding
    ) : MessageFileViewHolder(binding.root, binding.content, binding.date) {

        override fun bind(position: Int) {
            super.bind(position)
            bindAgent(position, binding.tvName, binding.avatar)
        }
    }

    internal inner class MessageImageAgentViewHolder(
            private val binding: UsedeskItemChatMessageImageAgentBinding
    ) : MessageImageViewHolder(binding.root, binding.content, binding.date) {

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