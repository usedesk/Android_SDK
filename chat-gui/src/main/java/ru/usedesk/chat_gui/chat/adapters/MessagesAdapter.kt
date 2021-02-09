package ru.usedesk.chat_gui.chat.adapters

import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


internal class MessagesAdapter(
        private val viewModel: ChatViewModel,
        private val recyclerView: RecyclerView,
        private val customAgentName: String?,
        private val onFileClick: (UsedeskFile) -> Unit,
        private val onUrlClick: (String) -> Unit
) : RecyclerView.Adapter<MessagesAdapter.BaseViewHolder>(), IUsedeskAdapter<ChatViewModel> {

    private var items: List<UsedeskMessage> = listOf()

    init {
        recyclerView.also {
            it.layoutManager = LinearLayoutManager(recyclerView.context)
            it.adapter = this
            it.setHasFixedSize(false)
            it.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                val difBottom = oldBottom - bottom
                if (difBottom > 0) {
                    it.scrollBy(0, difBottom)
                }
            }
        }
    }

    override fun onLiveData(viewModel: ChatViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.messagesLiveData.observe(lifecycleOwner) { messages ->
            onMessages(messages ?: listOf())
        }
    }

    private fun onMessages(messages: List<UsedeskMessage>) {
        val lastItems = items
        items = messages

        if (lastItems.isEmpty()) {
            notifyDataSetChanged()
            recyclerView.scrollToPosition(items.size - 1)
        } else {
            (lastItems.size until items.size).forEach { index ->
                notifyItemChanged(index - 1)
                notifyItemInserted(index)
            }
            val visibleBottom = recyclerView.computeVerticalScrollOffset() + recyclerView.height
            val contentHeight = recyclerView.computeVerticalScrollRange()
            if (visibleBottom >= contentHeight) {//Если чат был внизу
                recyclerView.scrollToPosition(items.size - 1)
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
                        R.style.Usedesk_Chat_Message_Text_Agent) { rootView, defaultStyleId ->
                    MessageTextAgentBinding(rootView, defaultStyleId)
                })
            }
            UsedeskMessage.Type.TYPE_AGENT_FILE.value -> {
                MessageFileAgentViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_file_agent,
                        R.style.Usedesk_Chat_Message_File_Agent) { rootView, defaultStyleId ->
                    MessageFileAgentBinding(rootView, defaultStyleId)
                })
            }
            UsedeskMessage.Type.TYPE_AGENT_IMAGE.value -> {
                MessageImageAgentViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_image_agent,
                        R.style.Usedesk_Chat_Message_Image_Agent) { rootView, defaultStyleId ->
                    MessageImageAgentBinding(rootView, defaultStyleId)
                })
            }
            UsedeskMessage.Type.TYPE_CLIENT_TEXT.value -> {
                MessageTextClientViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_text_client,
                        R.style.Usedesk_Chat_Message_Text_Client) { rootView, defaultStyleId ->
                    MessageTextClientBinding(rootView, defaultStyleId)
                })
            }
            UsedeskMessage.Type.TYPE_CLIENT_FILE.value -> {
                MessageFileClientViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_file_client,
                        R.style.Usedesk_Chat_Message_File_Client) { rootView, defaultStyleId ->
                    MessageFileClientBinding(rootView, defaultStyleId)
                })
            }
            UsedeskMessage.Type.TYPE_CLIENT_IMAGE.value -> {
                MessageImageClientViewHolder(inflateItem(parent,
                        R.layout.usedesk_item_chat_message_image_client,
                        R.style.Usedesk_Chat_Message_Image_Client) { rootView, defaultStyleId ->
                    MessageImageClientBinding(rootView, defaultStyleId)
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

        private val dateStyleValues = bindingDate.styleValues
                .getStyleValues(R.attr.usedesk_chat_message_date_text)

        override fun bind(position: Int) {
            val message = items[position]
            val formatted = getFormattedTime(message.createdAt)
            tvTime.text = formatted

            val previousMessage = items.getOrNull(position - 1)
            bindingDate.tvDate.visibility = if (isSameDay(previousMessage?.createdAt, message.createdAt)) {
                View.GONE
            } else {
                bindingDate.tvDate.text = when {
                    isToday(message.createdAt) -> {
                        dateStyleValues.getString(R.attr.usedesk_text_1)
                    }
                    isYesterday(message.createdAt) -> {
                        dateStyleValues.getString(R.attr.usedesk_text_2)
                    }
                    else -> {
                        val dateFormat: DateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())
                        val formatted = dateFormat.format(message.createdAt.time)
                        formatted
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
            agentBinding.tvName.visibility = visibleGone(!isSameAgent(messageAgent, position - 1))

            val avatarImageId = agentBinding.styleValues
                    .getStyleValues(R.attr.usedesk_chat_message_avatar_image)
                    .getId(R.attr.usedesk_drawable_1)

            setImage(agentBinding.ivAvatar, messageAgent.avatar, avatarImageId)
            agentBinding.ivAvatar.visibility = visibleInvisible(!isSameAgent(messageAgent, position + 1))

            val lastOfGroup = position == items.size - 1 ||
                    items.getOrNull(position + 1) is UsedeskMessageClient
            agentBinding.vEmpty.visibility = visibleGone(lastOfGroup)
        }

        fun bindClient(position: Int,
                       clientBinding: ClientBinding) {
            val lastOfGroup = position == items.size - 1 ||
                    items.getOrNull(position + 1) is UsedeskMessageAgent
            clientBinding.vEmpty.visibility = visibleGone(lastOfGroup)
        }

        private fun <T : Any> bindBottomMargin(
                vEmpty: View,
                isClient: Boolean
        ) {
            val last = if (isClient) {
                items.getOrNull(adapterPosition + 1) is UsedeskMessageAgent
            } else {
                items.getOrNull(adapterPosition + 1) is UsedeskMessageClient
            }
            vEmpty.visibility = visibleGone(last)
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

        private val loadingImageId = binding.styleValues
                .getStyleValues(R.attr.usedesk_chat_message_image_preview_image)
                .getId(R.attr.usedesk_drawable_1)

        override fun bind(position: Int) {
            super.bind(position)
            bindImage(position)
        }

        private fun bindImage(position: Int) {
            val messageFile = items[position] as UsedeskMessageFile

            binding.ivPreview.setOnClickListener(null)
            binding.ivError.setOnClickListener(null)

            showImage(binding.ivPreview,
                    loadingImageId,
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

        private val goodStyleValues = binding.styleValues
                .getStyleValues(R.attr.usedesk_chat_message_feedback_good_image)

        private val badStyleValues = binding.styleValues
                .getStyleValues(R.attr.usedesk_chat_message_feedback_bad_image)

        private val thanksText = binding.styleValues
                .getStyleValues(R.attr.usedesk_chat_message_text_message_text)
                .getString(R.attr.usedesk_text_1)

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

            binding.content.rootView.layoutParams.apply {
                width = if (messageAgentText.buttons.isEmpty()) {
                    FrameLayout.LayoutParams.WRAP_CONTENT
                } else {
                    FrameLayout.LayoutParams.MATCH_PARENT
                }
            }

            val ivLike = binding.content.ivLike
            val ivDislike = binding.content.ivDislike

            val goodImage = goodStyleValues.getId(R.attr.usedesk_drawable_1)
            val goodColoredImage = goodStyleValues.getId(R.attr.usedesk_drawable_2)
            val badImage = badStyleValues.getId(R.attr.usedesk_drawable_1)
            val badColoredImage = badStyleValues.getId(R.attr.usedesk_drawable_2)
            when {
                messageAgentText.feedback != null -> {
                    binding.content.lFeedback.visibility = View.VISIBLE

                    aloneSmile(ivLike,
                            binding.content.lFeedback,
                            goodColoredImage,
                            messageAgentText.feedback == UsedeskFeedback.LIKE
                    )

                    aloneSmile(ivDislike,
                            binding.content.lFeedback,
                            badColoredImage,
                            messageAgentText.feedback == UsedeskFeedback.DISLIKE
                    )

                    binding.content.tvText.text = thanksText
                }
                messageAgentText.feedbackNeeded -> {
                    binding.content.lFeedback.visibility = View.VISIBLE

                    enableSmile(ivLike,
                            ivDislike,
                            binding.content.lFeedback,
                            false,
                            goodImage,
                            goodColoredImage) {
                        viewModel.sendFeedback(messageAgentText, UsedeskFeedback.LIKE)
                        binding.content.tvText.text = thanksText
                    }

                    enableSmile(ivDislike,
                            ivLike,
                            binding.content.lFeedback,
                            true,
                            badImage,
                            badColoredImage) {
                        viewModel.sendFeedback(messageAgentText, UsedeskFeedback.DISLIKE)
                        binding.content.tvText.text = thanksText
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
                isClickable = false
                setOnClickListener(null)
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
                    isClickable = false
                    setOnClickListener(null)
                    imageViewSub.apply {
                        isEnabled = false
                        isClickable = false
                        setOnClickListener(null)
                    }

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

    internal class DateBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvDate: TextView = rootView.findViewById(R.id.tv_date)
    }

    internal class MessageTextBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)
        val rvButtons: RecyclerView = rootView.findViewById(R.id.rv_buttons)
        val lFeedback: ViewGroup = rootView.findViewById(R.id.l_feedback)
        val tvText: TextView = rootView.findViewById(R.id.tv_text)
        val ivLike: ImageView = rootView.findViewById(R.id.iv_like)
        val ivDislike: ImageView = rootView.findViewById(R.id.iv_dislike)
    }

    internal class MessageTextClientBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageTextBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val client = ClientBinding(rootView, defaultStyleId)
        val date = DateBinding(rootView, defaultStyleId)
    }

    internal class MessageTextAgentBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageTextBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val agent = AgentBinding(rootView, defaultStyleId)
        val date = DateBinding(rootView, defaultStyleId)
    }

    internal class MessageFileBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)
        val tvFileName: TextView = rootView.findViewById(R.id.tv_file_name)
        val tvFileSize: TextView = rootView.findViewById(R.id.tv_file_size)
        val tvExtension: TextView = rootView.findViewById(R.id.tv_extension)
    }

    internal class MessageFileClientBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageFileBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val client = ClientBinding(rootView, defaultStyleId)
        val date = DateBinding(rootView, defaultStyleId)
    }

    internal class MessageFileAgentBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageFileBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val agent = AgentBinding(rootView, defaultStyleId)
        val date = DateBinding(rootView, defaultStyleId)
    }

    internal class MessageImageBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)
        val ivPreview: RoundedImageView = rootView.findViewById(R.id.iv_preview)
        val ivError: ImageView = rootView.findViewById(R.id.iv_error)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
    }

    internal class MessageImageClientBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageImageBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val client = ClientBinding(rootView, defaultStyleId)
        val date = DateBinding(rootView, defaultStyleId)
    }

    internal class MessageImageAgentBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageImageBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val agent = AgentBinding(rootView, defaultStyleId)
        val date = DateBinding(rootView, defaultStyleId)
    }

    internal class AgentBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val ivAvatar: ImageView = rootView.findViewById(R.id.iv_avatar)
        val tvName: TextView = rootView.findViewById(R.id.tv_name)
        val vEmpty: View = rootView.findViewById(R.id.v_empty)
    }

    internal class ClientBinding(rootView: View, defaultStyleId: Int) : UsedeskBinding(rootView, defaultStyleId) {
        val vEmpty: View = rootView.findViewById(R.id.v_empty)
    }
}