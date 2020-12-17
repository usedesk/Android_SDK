package ru.usedesk.chat_gui.chat.adapters

import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.Corner
import com.makeramen.roundedimageview.RoundedImageView
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.ChatViewModel
import ru.usedesk.chat_sdk.data._entity.UsedeskFile
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.*
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
                        R.style.Usedesk_Chat_Message_Text_Agent) {
                    MessageTextAgentBinding(it)
                })
            }
            UsedeskMessage.Type.TYPE_AGENT_FILE.value -> {
                MessageFileAgentViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_file_agent,
                        R.style.Usedesk_Chat_Message_File_Agent) {
                    MessageFileAgentBinding(it)
                })
            }
            UsedeskMessage.Type.TYPE_AGENT_IMAGE.value -> {
                MessageImageAgentViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_image_agent,
                        R.style.Usedesk_Chat_Message_Image_Agent) {
                    MessageImageAgentBinding(it)
                })
            }
            UsedeskMessage.Type.TYPE_CLIENT_TEXT.value -> {
                MessageTextClientViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_text_client,
                        R.style.Usedesk_Chat_Message_Text_Client) {
                    MessageTextClientBinding(it)
                })
            }
            UsedeskMessage.Type.TYPE_CLIENT_FILE.value -> {
                MessageFileClientViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_file_client,
                        R.style.Usedesk_Chat_Message_File_Client) {
                    MessageFileClientBinding(it)
                })
            }
            UsedeskMessage.Type.TYPE_CLIENT_IMAGE.value -> {
                MessageImageClientViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_image_client,
                        R.style.Usedesk_Chat_Message_Image_Client) {
                    MessageImageClientBinding(it)
                })
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
            private val bindingDate: DateBinding,
            private val tvTime: TextView
    ) : BaseViewHolder(itemView) {

        override fun bind(position: Int) {
            val message = items[position]
            val formatted = getFormattedTime(message.calendar)
            tvTime.text = formatted

            val previousMessage = items.getOrNull(position + 1)
            bindingDate.tvDate.visibility = if (isSameDay(previousMessage?.calendar, message.calendar)) {
                View.GONE
            } else {
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
                View.VISIBLE
            }
        }

        private fun isSameDay(calendarA: Calendar?, calendarB: Calendar): Boolean {
            return calendarA?.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR) &&
                    calendarA.get(Calendar.DAY_OF_YEAR) == calendarB.get(Calendar.DAY_OF_YEAR)
        }

        fun bindAgent(position: Int,
                      agentBinding: AgentBinding) {
            val messageAgent = items[position] as UsedeskMessageAgent

            agentBinding.tvName.text = customAgentName ?: messageAgent.name
            agentBinding.tvName.visibility = visibleGone(!isSameAgent(messageAgent, position + 1))

            val initials = messageAgent.name.split(' ')
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .map { it[0] }
                    .joinToString(separator = "")
            agentBinding.avatar.tvAvatar.text = initials
            agentBinding.avatar.ivAvatar.setImageResource(if (initials.isEmpty()) {
                R.drawable.background_avatar_def
            } else {
                R.drawable.background_avatar_dark
            })

            setImage(agentBinding.avatar.ivAvatar, messageAgent.avatar, 0)
            agentBinding.avatar.rootView.visibility = visibleInvisible(!isSameAgent(messageAgent, position - 1))
        }

        fun bindClient(position: Int,
                       clientBinding: ClientBinding) {
            clientBinding.tvName.visibility = visibleGone(items.getOrNull(position + 1) is UsedeskMessageAgent)
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
            private val binding: MessageTextBinding,
            bindingDate: DateBinding)
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
            private val binding: MessageFileBinding,
            bindingDate: DateBinding
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
            binding.rootView.setOnClickListener {
                onFileClick(messageFile.file)
            }
        }
    }

    internal abstract inner class MessageImageViewHolder(
            itemView: View,
            private val binding: MessageImageBinding,
            bindingDate: DateBinding
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
            private val binding: MessageTextClientBinding
    ) : MessageTextViewHolder(binding.rootView, binding.content, binding.date) {
        override fun bind(position: Int) {
            super.bind(position)
            bindClient(position, binding.client)
        }
    }

    internal inner class MessageFileClientViewHolder(
            private val binding: MessageFileClientBinding
    ) : MessageFileViewHolder(binding.rootView, binding.content, binding.date) {
        override fun bind(position: Int) {
            super.bind(position)
            bindClient(position, binding.client)
        }
    }

    internal inner class MessageImageClientViewHolder(
            private val binding: MessageImageClientBinding
    ) : MessageImageViewHolder(binding.rootView, binding.content, binding.date) {
        override fun bind(position: Int) {
            super.bind(position)
            bindClient(position, binding.client)

            binding.content.ivPreview.setCornerRadius(Corner.BOTTOM_RIGHT, 0.0f)
        }
    }

    internal inner class MessageTextAgentViewHolder(
            private val binding: MessageTextAgentBinding
    ) : MessageTextViewHolder(binding.rootView, binding.content, binding.date) {

        private val buttonsAdapter = ButtonsAdapter(binding.content.rvButtons) {
            if (it.url.isNotEmpty()) {
                onUrlClick(it.url)
            } else {
                viewModel.onSend(it.text)
            }
        }

        override fun bind(position: Int) {
            super.bind(position)
            bindAgent(position, binding.agent)

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
            private val binding: MessageFileAgentBinding
    ) : MessageFileViewHolder(binding.rootView, binding.content, binding.date) {

        override fun bind(position: Int) {
            super.bind(position)
            bindAgent(position, binding.agent)
        }
    }

    internal inner class MessageImageAgentViewHolder(
            private val binding: MessageImageAgentBinding
    ) : MessageImageViewHolder(binding.rootView, binding.content, binding.date) {

        override fun bind(position: Int) {
            super.bind(position)
            bindAgent(position, binding.agent)

            binding.content.ivPreview.setCornerRadius(Corner.BOTTOM_LEFT, 0.0f)
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

    internal class DateBinding(rootView: View) : UsedeskBinding(rootView) {
        val tvDate: TextView = rootView.findViewById(R.id.tv_date)
    }

    internal class AvatarBinding(rootView: View) : UsedeskBinding(rootView) {
        val ivAvatar: ImageView = rootView.findViewById(R.id.iv_avatar)
        val tvAvatar: TextView = rootView.findViewById(R.id.tv_avatar)
    }

    internal class MessageTextBinding(rootView: View) : UsedeskBinding(rootView) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)
        val rvButtons: RecyclerView = rootView.findViewById(R.id.rv_buttons)
        val lFeedback: ViewGroup = rootView.findViewById(R.id.l_feedback)
        val tvText: TextView = rootView.findViewById(R.id.tv_text)
        val tvLink: TextView = rootView.findViewById(R.id.tv_link)
        val ivLike: ImageView = rootView.findViewById(R.id.iv_like)
        val ivDislike: ImageView = rootView.findViewById(R.id.iv_dislike)
    }

    internal class MessageTextClientBinding(rootView: View) : UsedeskBinding(rootView) {
        val content = MessageTextBinding(rootView.findViewById(R.id.content))
        val client = ClientBinding(rootView)
        val date = DateBinding(rootView)
    }

    internal class MessageTextAgentBinding(rootView: View) : UsedeskBinding(rootView) {
        val content = MessageTextBinding(rootView.findViewById(R.id.content))
        val agent = AgentBinding(rootView)
        val date = DateBinding(rootView)
    }

    internal class MessageFileBinding(rootView: View) : UsedeskBinding(rootView) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)
        val tvFileName: TextView = rootView.findViewById(R.id.tv_file_name)
        val tvFileSize: TextView = rootView.findViewById(R.id.tv_file_size)
        val tvExtension: TextView = rootView.findViewById(R.id.tv_extension)
    }

    internal class MessageFileClientBinding(rootView: View) : UsedeskBinding(rootView) {
        val content = MessageFileBinding(rootView.findViewById(R.id.content))
        val client = ClientBinding(rootView)
        val date = DateBinding(rootView)
    }

    internal class MessageFileAgentBinding(rootView: View) : UsedeskBinding(rootView) {
        val content = MessageFileBinding(rootView.findViewById(R.id.content))
        val agent = AgentBinding(rootView)
        val date = DateBinding(rootView)
    }

    internal class MessageImageBinding(rootView: View) : UsedeskBinding(rootView) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)
        val ivPreview: RoundedImageView = rootView.findViewById(R.id.iv_preview)
        val ivError: ImageView = rootView.findViewById(R.id.iv_error)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
    }

    internal class MessageImageClientBinding(rootView: View) : UsedeskBinding(rootView) {
        val content = MessageImageBinding(rootView.findViewById(R.id.content))
        val client = ClientBinding(rootView)
        val date = DateBinding(rootView)
    }

    internal class MessageImageAgentBinding(rootView: View) : UsedeskBinding(rootView) {
        val content = MessageImageBinding(rootView.findViewById(R.id.content))
        val agent = AgentBinding(rootView)
        val date = DateBinding(rootView)
    }

    internal class AgentBinding(rootView: View) : UsedeskBinding(rootView) {
        val avatar = AvatarBinding(rootView.findViewById(R.id.avatar))
        val tvName: TextView = rootView.findViewById(R.id.tv_name)
    }

    internal class ClientBinding(rootView: View) : UsedeskBinding(rootView) {
        val tvName: TextView = rootView.findViewById(R.id.tv_name)
    }
}