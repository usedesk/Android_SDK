package ru.usedesk.chat_gui.chat.messages.adapters

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.*
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.makeramen.roundedimageview.RoundedImageView
import ru.usedesk.chat_gui.NoMoveLinkMovementMethod
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.MediaPlayerAdapter
import ru.usedesk.chat_gui.chat.messages.DateBinding
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.*
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_gui.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max


internal class MessagesAdapter(
    private val recyclerView: RecyclerView,
    private val dateBinding: DateBinding,
    private val viewModel: MessagesViewModel,
    lifecycleOwner: LifecycleOwner,
    private val customAgentName: String?,
    private val rejectedFileExtensions: Array<String>,
    private val mediaPlayerAdapter: MediaPlayerAdapter,
    private val onFileClick: (UsedeskFile) -> Unit,
    private val onUrlClick: (String) -> Unit,
    private val onFileDownloadClick: (UsedeskFile) -> Unit,
    messagesDateFormat: String,
    messageTimeFormat: String,
    private val adaptiveTextMessageTimePadding: Boolean,
    savedStated: Bundle?
) : RecyclerView.Adapter<MessagesAdapter.BaseViewHolder>() {

    private var items: List<ChatItem> = listOf()
    private val layoutManager = LinearLayoutManager(recyclerView.context).apply {
        reverseLayout = true
    }

    private val saved = savedStated != null

    private val dateFormat = SimpleDateFormat(messagesDateFormat, Locale.getDefault())
    private val timeFormat = SimpleDateFormat(messageTimeFormat, Locale.getDefault())

    private val dateStyleValues = dateBinding.styleValues
        .getStyleValues(R.attr.usedesk_chat_message_date_text)
    private val todayText = dateStyleValues.getString(R.attr.usedesk_text_1)
    private val yesterdayText = dateStyleValues.getString(R.attr.usedesk_text_2)

    private val timeFormatText = timeFormat.format(Calendar.getInstance().apply {
        set(Calendar.YEAR, 9999)
        set(Calendar.HOUR, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.time)

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT
        recyclerView.apply {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            layoutManager = this@MessagesAdapter.layoutManager
            adapter = this@MessagesAdapter
            setHasFixedSize(false)
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                val difBottom = oldBottom - bottom
                if (difBottom > 0) {
                    scrollBy(0, difBottom)
                }
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy != 0) {
                        updateToBottomButton()
                        updateFloatingDate()
                        updateFirstVisibleIndex()
                    }
                }
            })
        }
        viewModel.modelLiveData.initAndObserveWithOld(lifecycleOwner) { old, new ->
            if (old?.chatItems != new.chatItems) {
                onMessages(new.chatItems)
            }
        }
        updateToBottomButton()
        updateFloatingDate()
        updateFirstVisibleIndex()
    }

    private fun updateFirstVisibleIndex() {
        val topItemIndex = layoutManager.findLastVisibleItemPosition()
        if (topItemIndex >= 0) {
            val bottomItemIndex = layoutManager.findFirstVisibleItemPosition()
            viewModel.onMessagesShowed(bottomItemIndex..topItemIndex)
        }
    }

    fun updateFloatingDate() {
        dateBinding.tvDate.post {
            val firstIndex = layoutManager.findFirstVisibleItemPosition()

            dateBinding.rootView.y = 0f
            dateBinding.rootView.visibility = View.INVISIBLE

            items.indices.asSequence().mapNotNull { i ->
                recyclerView.findViewHolderForAdapterPosition(i)
            }.filterIsInstance<DateViewHolder>().forEach {
                it.binding.rootView.visibility = View.VISIBLE
            }

            /**
             * notVisible - тот, что не виден полностью (или весь в зоне floating)
             * top - тот что виден полностью, или вышел из зоны floating снизу
             *
             * Если есть notVisible, то дата равна ему, а если нет, то дата равна top
             * Если дата равна notVisible, то если top в зоне floating, то дата сдвигается вверх
             */

            if (firstIndex >= 0) {
                val topIndex = layoutManager.findLastVisibleItemPosition()
                val topDateIndex = (firstIndex..topIndex).lastOrNull { i ->
                    items.getOrNull(i) is ChatDate
                } ?: -1
                val topDateHolder = recyclerView.findViewHolderForAdapterPosition(
                    topDateIndex
                ) as? DateViewHolder

                val floatingDateParentRect = dateBinding.rootView.makeGlobalVisibleRect()
                val topDateRect = topDateHolder?.binding?.rootView?.makeGlobalVisibleRect()
                val notVisibleDateIndex = if (topDateRect != null &&
                    topDateRect.bottom <= floatingDateParentRect.bottom
                ) {
                    topDateIndex
                } else {
                    (topIndex + 1 until itemCount).firstOrNull { i ->
                        items.getOrNull(i) is ChatDate
                    } ?: -1
                }
                val notVisibleDate = items.getOrNull(notVisibleDateIndex) as? ChatDate
                val topDate = items.getOrNull(topDateIndex) as? ChatDate
                val targetDate = notVisibleDate ?: topDate
                if (targetDate != null) {
                    val text = getDateText(targetDate)
                    dateBinding.tvDate.text = text
                    dateBinding.rootView.visibility = View.VISIBLE
                    dateBinding.rootView.y = if (topDateRect != null &&
                        topDateRect.top < floatingDateParentRect.bottom &&
                        topDateRect.bottom > floatingDateParentRect.bottom
                    ) {
                        topDateRect.top - floatingDateParentRect.bottom
                    } else {
                        0
                    }.toFloat()

                    (recyclerView.findViewHolderForAdapterPosition(
                        if (targetDate == notVisibleDate) notVisibleDateIndex
                        else topDateIndex
                    ) as? DateViewHolder)?.binding?.rootView?.visibility = View.INVISIBLE
                }
            }
            dateBinding.rootView.requestLayout()
        }
    }

    private fun View.makeGlobalVisibleRect() = Rect().also {
        this.getGlobalVisibleRect(it)
    }

    private fun updateToBottomButton() {
        viewModel.showToBottomButton(
            recyclerView.height > 0 &&
                    items.isNotEmpty() &&
                    !isAtBottom()
        )
    }

    private fun isAtBottom(): Boolean {
        val visibleBottom = recyclerView.computeVerticalScrollOffset() + recyclerView.height
        val contentHeight = recyclerView.computeVerticalScrollRange()
        return visibleBottom >= contentHeight
    }


    private fun ChatMessage.isIdEquals(otherMessage: ChatMessage): Boolean {
        return (message.id == otherMessage.message.id) ||
                (message is UsedeskMessageClient &&
                        otherMessage.message is UsedeskMessageClient &&
                        message.localId == otherMessage.message.localId)
    }

    private fun onMessages(messages: List<ChatItem>) {
        val oldItems = items
        items = messages

        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getChangePayload(
                oldItemPosition: Int,
                newItemPosition: Int
            ) = oldItems[oldItemPosition]

            override fun getOldListSize() = oldItems.size

            override fun getNewListSize() = items.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = oldItems[oldItemPosition]
                val new = items[newItemPosition]
                return when (new) {
                    is ChatDate -> old is ChatDate &&
                            old.calendar.timeInMillis == new.calendar.timeInMillis
                    is ChatLoading -> old is ChatLoading
                    is ChatMessage -> old is ChatMessage && old.isIdEquals(new)
                    is MessageAgentName -> old is MessageAgentName && old.name == new.name
                }
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = oldItems[oldItemPosition]
                val new = items[newItemPosition]
                val result = when (new) {
                    is ChatDate,
                    is ChatLoading,
                    is MessageAgentName -> true
                    is ChatMessage -> when {
                        old !is ChatMessage -> false
                        (new.isLastOfGroup != old.isLastOfGroup) -> false
                        (new.message as? UsedeskMessageText)?.convertedText !=
                                (old.message as? UsedeskMessageText)?.convertedText -> false
                        (new.message as? UsedeskMessageFile)?.file?.content !=
                                (old.message as? UsedeskMessageFile)?.file?.content -> false
                        (new.message as? UsedeskMessageClient)?.status !=
                                (old.message as? UsedeskMessageClient)?.status -> false
                        (new as? AgentMessage)?.showAvatar !=
                                (old as? AgentMessage)?.showAvatar -> false
                        (new as? AgentMessage)?.showName !=
                                (old as? AgentMessage)?.showName -> false
                        else -> true
                    }
                }
                return result
            }
        }).dispatchUpdatesTo(this)

        if (messages.isNotEmpty() || stateRestorationPolicy != StateRestorationPolicy.ALLOW) {
            recyclerView.post {
                stateRestorationPolicy = StateRestorationPolicy.ALLOW
                recyclerView.post {
                    updateToBottomButton()
                    updateFloatingDate()
                    updateFirstVisibleIndex()
                }
            }
        }
        var isScrollToBottom = false
        if (oldItems.isEmpty()) {
            if (!saved) {
                isScrollToBottom = true
            }
        } else {
            isScrollToBottom = isAtBottom()
        }
        if (isScrollToBottom) {
            recyclerView.scrollToPosition(0)
        } else {
            updateToBottomButton()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            R.layout.usedesk_item_chat_date ->
                DateViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Date
                ) { rootView, defaultStyleId ->
                    DateBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_loading ->
                LoadingViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Loading
                ) { rootView, defaultStyleId ->
                    UsedeskBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_text_agent ->
                MessageTextAgentViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_Text_Agent
                ) { rootView, defaultStyleId ->
                    MessageTextAgentBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_file_agent ->
                MessageFileAgentViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_File_Agent
                ) { rootView, defaultStyleId ->
                    MessageFileAgentBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_image_agent ->
                MessageImageAgentViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_Image_Agent
                ) { rootView, defaultStyleId ->
                    MessageImageAgentBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_video_agent ->
                MessageVideoAgentViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_Video_Agent
                ) { rootView, defaultStyleId ->
                    MessageVideoAgentBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_audio_agent ->
                MessageAudioAgentViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_Audio_Agent
                ) { rootView, defaultStyleId ->
                    MessageAudioAgentBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_text_client ->
                MessageTextClientViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_Text_Client
                ) { rootView, defaultStyleId ->
                    MessageTextClientBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_file_client ->
                MessageFileClientViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_File_Client
                ) { rootView, defaultStyleId ->
                    MessageFileClientBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_image_client ->
                MessageImageClientViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_Image_Client
                ) { rootView, defaultStyleId ->
                    MessageImageClientBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_video_client ->
                MessageVideoClientViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_Video_Client
                ) { rootView, defaultStyleId ->
                    MessageVideoClientBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_audio_client ->
                MessageAudioClientViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_Audio_Client
                ) { rootView, defaultStyleId ->
                    MessageAudioClientBinding(rootView, defaultStyleId)
                })
            R.layout.usedesk_item_chat_message_agent_name ->
                MessageAgentNameViewHolder(inflateItem(
                    parent,
                    viewType,
                    R.style.Usedesk_Chat_Message_Text_Agent
                ) { rootView, defaultStyleId ->
                    MessageAgentNameBinding(rootView, defaultStyleId)
                })
            else -> throw RuntimeException("Unknown view type:$viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ChatDate -> R.layout.usedesk_item_chat_date
            is ChatLoading -> R.layout.usedesk_item_chat_loading
            is MessageAgentName -> R.layout.usedesk_item_chat_message_agent_name
            is ChatMessage -> when (item.message) {
                is UsedeskMessageAgentText -> R.layout.usedesk_item_chat_message_text_agent
                is UsedeskMessageAgentImage -> R.layout.usedesk_item_chat_message_image_agent
                is UsedeskMessageAgentVideo -> R.layout.usedesk_item_chat_message_video_agent
                is UsedeskMessageAgentAudio -> R.layout.usedesk_item_chat_message_audio_agent
                is UsedeskMessageAgentFile -> R.layout.usedesk_item_chat_message_file_agent
                is UsedeskMessageClientText -> R.layout.usedesk_item_chat_message_text_client
                is UsedeskMessageClientImage -> R.layout.usedesk_item_chat_message_image_client
                is UsedeskMessageClientVideo -> R.layout.usedesk_item_chat_message_video_client
                is UsedeskMessageClientAudio -> R.layout.usedesk_item_chat_message_audio_client
                is UsedeskMessageClientFile -> R.layout.usedesk_item_chat_message_file_client
            }
        }
    }

    fun scrollToBottom() {
        recyclerView.smoothScrollToPosition(0)
    }

    fun isVisibleChild(child: View): Boolean {
        val rectParent = Rect()
        recyclerView.getGlobalVisibleRect(rectParent)
        val rectItem = Rect()
        child.getGlobalVisibleRect(rectItem)
        return rectParent.contains(rectItem)
    }

    internal abstract class BaseViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(chatItem: ChatItem)
    }

    internal abstract inner class MessageViewHolder(
        itemView: View,
        private val tvTime: TextView,
        private val styleValues: UsedeskResourceManager.StyleValues,
        private val isClient: Boolean
    ) : BaseViewHolder(itemView) {

        private val sendingDrawable: Drawable? = getDrawableIcon(R.attr.usedesk_drawable_1)
        private val successfullyDrawable: Drawable? = getDrawableIcon(R.attr.usedesk_drawable_2)

        private fun getDrawableIcon(attrId: Int): Drawable? {
            val id = styleValues.getStyleValues(R.attr.usedesk_chat_message_time_text)
                .getIdOrZero(attrId)
            return if (id != 0) {
                tvTime.resources.getDrawable(id)
            } else {
                null
            }
        }

        protected fun getAdaptiveMargin(
            tvTime: TextView,
            content: View
        ): Int {
            content.apply {
                val bounds = Rect()
                tvTime.paint.getTextBounds(
                    timeFormatText,
                    0,
                    timeFormatText.length,
                    bounds
                )
                val iconWidth = if (isClient) {
                    val maxWidth = max(
                        sendingDrawable?.intrinsicWidth ?: 0,
                        successfullyDrawable?.intrinsicWidth ?: 0
                    )
                    if (maxWidth > 0) {
                        maxWidth + tvTime.compoundDrawablePadding
                    } else {
                        0
                    }
                } else {
                    0
                }
                return tvTime.marginLeft +
                        tvTime.marginRight +
                        content.paddingRight +
                        bounds.width() +
                        iconWidth
            }
        }

        override fun bind(chatItem: ChatItem) {
            chatItem as ChatMessage
            val formatted = timeFormat.format(chatItem.message.createdAt.time)
            tvTime.text = formatted
        }

        fun bindAgent(
            chatItem: ChatItem,
            agentBinding: AgentBinding
        ) {
            val messageAgent = (chatItem as AgentMessage).message as UsedeskMessageAgent

            agentBinding.tvName.text = customAgentName ?: messageAgent.name
            agentBinding.tvName.visibility = visibleGone(chatItem.showName)

            val avatarImageId: Int
            val visibleState: Int
            val invisibleState: Int

            agentBinding.styleValues.getStyleValues(R.attr.usedesk_chat_message_avatar_image).run {
                avatarImageId = getId(R.attr.usedesk_drawable_1)
                val visibility = listOf(View.VISIBLE, View.INVISIBLE, View.GONE)
                    .getOrNull(getInt(android.R.attr.visibility))
                when (visibility) {
                    View.INVISIBLE -> {
                        visibleState = View.INVISIBLE
                        invisibleState = View.INVISIBLE
                    }
                    View.GONE -> {
                        visibleState = View.GONE
                        invisibleState = View.GONE
                    }
                    else -> {
                        visibleState = View.VISIBLE
                        invisibleState = View.INVISIBLE
                        setImage(agentBinding.ivAvatar, messageAgent.avatar, avatarImageId)
                    }
                }
            }

            agentBinding.ivAvatar.visibility = when {
                chatItem.showAvatar -> visibleState
                else -> invisibleState
            }
            agentBinding.vEmpty.visibility = visibleGone(chatItem.isLastOfGroup)
        }

        fun bindClient(
            chatItem: ChatItem,
            clientBinding: ClientBinding
        ) {
            val clientMessage = (chatItem as ChatMessage).message as UsedeskMessageClient
            val statusDrawable = when (clientMessage.status) {
                UsedeskMessageClient.Status.SUCCESSFULLY_SENT -> successfullyDrawable
                else -> sendingDrawable
            }

            tvTime.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                statusDrawable,
                null
            )

            clientBinding.apply {
                ivSentFailed.apply {
                    visibility =
                        visibleInvisible(clientMessage.status == UsedeskMessageClient.Status.SEND_FAILED)
                    setOnClickListener {
                        PopupMenu(it.context, it).apply {
                            inflate(R.menu.usedesk_messages_error_popup)
                            setOnMenuItemClickListener { item ->
                                when (item.itemId) {
                                    R.id.send_again -> {
                                        viewModel.sendAgain(clientMessage.localId)
                                    }
                                    R.id.remove_message -> {
                                        viewModel.removeMessage(clientMessage.localId)
                                    }
                                }
                                true
                            }
                            show()
                        }
                    }
                }
                vEmpty.visibility = visibleGone(chatItem.isLastOfGroup)
            }
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
    }

    internal abstract inner class MessageTextViewHolder(
        itemView: View,
        private val binding: MessageTextBinding,
        isClient: Boolean
    ) : MessageViewHolder(itemView, binding.tvTime, binding.styleValues, isClient) {

        init {
            if (adaptiveTextMessageTimePadding) {
                val adaptiveMargin = getAdaptiveMargin(
                    binding.tvTime,
                    binding.lContent
                )
                binding.lContent.run {
                    setPadding(
                        paddingLeft,
                        paddingTop,
                        adaptiveMargin,
                        paddingBottom
                    )
                }
            }
            binding.tvText.run {
                isClickable = true
                movementMethod = NoMoveLinkMovementMethod
            }
        }

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)

            binding.lFeedback.visibility = View.GONE

            val messageText = (chatItem as ChatMessage).message as UsedeskMessageText

            binding.tvText.run {
                text = Html.fromHtml(messageText.convertedText + " ") //TODO: temp fix
                visibility = View.VISIBLE
            }
        }
    }

    internal abstract inner class MessageFileViewHolder(
        itemView: View,
        private val binding: MessageFileBinding,
        isClient: Boolean
    ) : MessageViewHolder(itemView, binding.tvTime, binding.styleValues, isClient) {

        private val textSizeStyleValues =
            binding.styleValues.getStyleValues(R.attr.usedesk_chat_message_file_size_text)

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)

            val messageFile = (chatItem as ChatMessage).message as UsedeskMessageFile

            val name = messageFile.file.name
            binding.tvFileName.text = name
            binding.tvExtension.text = name.substringAfterLast('.')
            if (rejectedFileExtensions.any { name.endsWith(it) }) {
                val textColor = textSizeStyleValues.getColor(R.attr.usedesk_text_color_2)
                binding.tvFileSize.text = textSizeStyleValues.getString(R.attr.usedesk_text_1)
                binding.tvFileSize.setTextColor(textColor)
            } else {
                val textColor = textSizeStyleValues.getColor(R.attr.usedesk_text_color_1)
                binding.tvFileSize.text = messageFile.file.size
                binding.tvFileSize.setTextColor(textColor)
            }
            binding.rootView.setOnClickListener {
                onFileClick(messageFile.file)
            }
        }
    }

    internal abstract inner class MessageImageViewHolder(
        itemView: View,
        private val binding: MessageImageBinding,
        isClient: Boolean
    ) : MessageViewHolder(itemView, binding.tvTime, binding.styleValues, isClient) {

        private var oldItem: ChatMessage? = null

        private val loadingImageId = binding.styleValues
            .getStyleValues(R.attr.usedesk_chat_message_image_preview_image)
            .getId(R.attr.usedesk_drawable_1)

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindImage(chatItem)
        }

        private fun bindImage(chatItem: ChatItem) {
            chatItem as ChatMessage
            val messageFile = chatItem.message as UsedeskMessageFile

            if (oldItem?.isIdEquals(chatItem) != true) {
                clearImage(binding.ivPreview)

                binding.ivPreview.setOnClickListener(null)
                binding.ivError.setOnClickListener(null)

                showImage(binding.ivPreview,
                    messageFile.file.content,
                    loadingImageId,
                    binding.pbLoading,
                    binding.ivError, {
                        binding.ivPreview.setOnClickListener {
                            onFileClick(messageFile.file)
                        }
                    }, {
                        binding.ivError.setOnClickListener {
                            bindImage(chatItem)
                        }
                    })
            } else {
                showImage(
                    binding.ivPreview,
                    messageFile.file.content,
                    vError = binding.ivError,
                    onSuccess = {
                        binding.pbLoading.visibility = View.INVISIBLE
                        binding.ivPreview.setOnClickListener {
                            onFileClick(messageFile.file)
                        }
                    },
                    onError = {
                        binding.pbLoading.visibility = View.INVISIBLE
                        binding.ivError.setOnClickListener {
                            bindImage(chatItem)
                        }
                    },
                    oldPlaceholder = true
                )
            }

            oldItem = chatItem
        }
    }

    internal abstract inner class MessageVideoViewHolder(
        itemView: View,
        private val binding: MessageVideoBinding,
        isClient: Boolean
    ) : MessageViewHolder(itemView, binding.tvTime, binding.styleValues, isClient) {

        private lateinit var usedeskFile: UsedeskFile
        private var lastVisible = false

        private val defaultTimeBottomPadding = binding.tvTime.marginBottom

        init {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val visible = isVisibleChild(binding.rootView)
                    if (!visible && lastVisible) {
                        mediaPlayerAdapter.detachPlayer(usedeskFile.content)
                    }
                    lastVisible = visible
                }
            })
        }

        private fun bindVideo(messageFile: UsedeskMessageFile) {
            this.usedeskFile = messageFile.file

            changeElements(
                showStub = true,
                showPlay = true,
            )

            val doOnCancelPlay = {
                showPreview()
            }

            val doOnControlsVisibilityChanged: ((Int) -> Unit) = { height ->
                binding.tvTime.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    updateMargins(bottom = defaultTimeBottomPadding + height)
                }
            }

            binding.ivPlay.setOnClickListener {
                mediaPlayerAdapter.attachPlayer(
                    binding.lVideo,
                    usedeskFile.content,
                    usedeskFile.name,
                    MediaPlayerAdapter.PlayerType.VIDEO,
                    doOnCancelPlay,
                    doOnControlsVisibilityChanged
                )
                changeElements(showVideo = true)
            }

            if (mediaPlayerAdapter.reattachPlayer(
                    binding.lVideo,
                    usedeskFile.content,
                    doOnCancelPlay,
                    doOnControlsVisibilityChanged
                )
            ) {
                changeElements(showVideo = true)
            }
        }

        private fun showPreview() {
            changeElements(
                showStub = true,
                showPlay = true,
            )
            /*//TODO: Не раскомменчивать до весны
            GlideUtil.showThumbnail(binding.ivPreview,
                usedeskFile.content,
                onSuccess = {
                    if (binding.pvVideo.visibility != View.VISIBLE) {
                        changeElements(
                            showPreview = true,
                            showPlay = true
                        )
                    }
                }
            )*/
        }

        private fun changeElements(
            showStub: Boolean = false,
            showPreview: Boolean = false,
            showPlay: Boolean = false,
            showVideo: Boolean = false
        ) {
            binding.lStub.visibility = visibleInvisible(showStub)
            binding.ivPreview.visibility = visibleInvisible(showPreview)
            binding.ivPlay.visibility = visibleInvisible(showPlay)
            binding.lVideo.visibility = visibleInvisible(showVideo)
        }

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindVideo((chatItem as ChatMessage).message as UsedeskMessageFile)
        }
    }

    internal abstract inner class MessageAudioViewHolder(
        itemView: View,
        private val binding: MessageAudioBinding,
        isClient: Boolean
    ) : MessageViewHolder(itemView, binding.tvTime, binding.styleValues, isClient) {

        private var usedeskFile = UsedeskFile("", "", "", "")
        private var lastVisible = false

        init {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val visible = isVisibleChild(binding.rootView)
                    if (!visible && lastVisible) {
                        mediaPlayerAdapter.detachPlayer(usedeskFile.content)
                    }
                    lastVisible = visible
                }
            })
            binding.stubProgress.visibility = View.VISIBLE
            binding.stubScrubber.visibility = View.VISIBLE
            binding.ivExoPause.visibility = View.INVISIBLE

            val adaptiveMargin = getAdaptiveMargin(
                binding.tvTime,
                binding.tvDownload
            )
            binding.tvDownload.run {
                setPadding(
                    paddingLeft,
                    paddingTop,
                    adaptiveMargin,
                    paddingBottom
                )
            }
        }

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)

            val audioMessage = (chatItem as ChatMessage).message as UsedeskMessageFile
            bindAudio(audioMessage)
        }

        private fun bindAudio(messageFile: UsedeskMessageFile) {
            this.usedeskFile = messageFile.file

            changeElements(
                stub = true
            )

            binding.exoPosition.text = ""

            binding.tvDownload.setOnClickListener {
                onFileDownloadClick(usedeskFile)
            }

            val doOnCancel = {
                changeElements(
                    stub = true
                )
            }

            binding.ivExoPlay.setOnClickListener {
                mediaPlayerAdapter.attachPlayer(
                    binding.lAudio,
                    usedeskFile.content,
                    usedeskFile.name,
                    MediaPlayerAdapter.PlayerType.AUDIO,
                    doOnCancel
                )
                changeElements()
            }

            if (mediaPlayerAdapter.reattachPlayer(
                    binding.lAudio,
                    usedeskFile.content,
                    doOnCancel
                )
            ) {
                changeElements()
            }
        }

        private fun changeElements(
            stub: Boolean = false
        ) {
            binding.lStub.visibility = visibleGone(stub)
        }
    }

    internal inner class MessageTextClientViewHolder(
        private val binding: MessageTextClientBinding
    ) : MessageTextViewHolder(binding.rootView, binding.content, true) {

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindClient(chatItem, binding.client)
        }
    }

    internal inner class MessageFileClientViewHolder(
        private val binding: MessageFileClientBinding
    ) : MessageFileViewHolder(binding.rootView, binding.content, true) {
        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindClient(chatItem, binding.client)
        }
    }

    internal inner class MessageImageClientViewHolder(
        private val binding: MessageImageClientBinding
    ) : MessageImageViewHolder(binding.rootView, binding.content, true) {
        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindClient(chatItem, binding.client)
        }
    }

    internal inner class MessageVideoClientViewHolder(
        private val binding: MessageVideoClientBinding
    ) : MessageVideoViewHolder(binding.rootView, binding.content, true) {
        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindClient(chatItem, binding.client)
        }
    }

    internal inner class MessageAudioClientViewHolder(
        private val binding: MessageAudioClientBinding
    ) : MessageAudioViewHolder(binding.rootView, binding.content, true) {

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindClient(chatItem, binding.client)
        }
    }

    internal inner class DateViewHolder(
        val binding: DateBinding
    ) : BaseViewHolder(binding.rootView) {

        override fun bind(chatItem: ChatItem) {
            binding.tvDate.text = getDateText(chatItem as ChatDate)
            binding.rootView.visibility = View.VISIBLE
        }
    }

    internal inner class LoadingViewHolder(
        val binding: UsedeskBinding
    ) : BaseViewHolder(binding.rootView) {

        override fun bind(chatItem: ChatItem) {}
    }

    private fun getDateText(chatDate: ChatDate): String {
        return when {
            isToday(chatDate.calendar) -> todayText
            isYesterday(chatDate.calendar) -> yesterdayText
            else -> dateFormat.format(chatDate.calendar.time)
        }
    }

    internal inner class MessageTextAgentViewHolder(
        private val binding: MessageTextAgentBinding
    ) : MessageTextViewHolder(binding.rootView, binding.content, false) {

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
                viewModel.onSendButton(it.text)
            }
        }

        private val goodAtStart = binding.styleValues
            .getStyleValues(R.attr.usedesk_chat_message_feedback_good_image)
            .getInt(android.R.attr.layout_gravity) in arrayOf(Gravity.START, Gravity.LEFT)

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindAgent(chatItem, binding.agent)

            val messageAgentText = (chatItem as AgentMessage).message as UsedeskMessageAgentText
            buttonsAdapter.update(messageAgentText.buttons)

            binding.content.rootView.layoutParams.apply {
                width = if (messageAgentText.buttons.isEmpty()
                    && !messageAgentText.feedbackNeeded
                    && messageAgentText.feedback == null
                ) {
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

                    aloneSmile(
                        ivLike,
                        binding.content.lFeedback,
                        goodColoredImage,
                        messageAgentText.feedback == UsedeskFeedback.LIKE
                    )

                    aloneSmile(
                        ivDislike,
                        binding.content.lFeedback,
                        badColoredImage,
                        messageAgentText.feedback == UsedeskFeedback.DISLIKE
                    )

                    binding.content.tvText.text = thanksText
                }
                messageAgentText.feedbackNeeded -> {
                    binding.content.lFeedback.visibility = View.VISIBLE

                    enableSmile(
                        ivLike,
                        ivDislike,
                        binding.content.lFeedback,
                        goodAtStart,
                        goodImage,
                        goodColoredImage
                    ) {
                        viewModel.sendFeedback(messageAgentText, UsedeskFeedback.LIKE)
                        binding.content.tvText.text = thanksText
                    }

                    enableSmile(
                        ivDislike,
                        ivLike,
                        binding.content.lFeedback,
                        !goodAtStart,
                        badImage,
                        badColoredImage
                    ) {
                        viewModel.sendFeedback(messageAgentText, UsedeskFeedback.DISLIKE)
                        binding.content.tvText.text = thanksText
                    }
                }
                else -> {
                    binding.content.lFeedback.visibility = View.GONE
                }
            }
        }

        private fun aloneSmile(
            imageView: ImageView,
            container: ViewGroup,
            imageId: Int,
            visible: Boolean
        ) {
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

        private fun enableSmile(
            imageViewMain: ImageView,
            imageViewSub: ImageView,
            container: ViewGroup,
            initStart: Boolean,
            initImageId: Int,
            activeImageId: Int,
            onClick: () -> Unit
        ) {
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

    internal inner class MessageAgentNameViewHolder(
        private val binding: MessageAgentNameBinding
    ) : BaseViewHolder(binding.rootView) {

        override fun bind(chatItem: ChatItem) {
            chatItem as MessageAgentName
            binding.tvName.text = chatItem.name
        }
    }

    internal inner class MessageFileAgentViewHolder(
        private val binding: MessageFileAgentBinding
    ) : MessageFileViewHolder(binding.rootView, binding.content, false) {

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindAgent(chatItem, binding.agent)
        }
    }

    internal inner class MessageImageAgentViewHolder(
        private val binding: MessageImageAgentBinding
    ) : MessageImageViewHolder(binding.rootView, binding.content, false) {

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindAgent(chatItem, binding.agent)
        }
    }

    internal inner class MessageVideoAgentViewHolder(
        private val binding: MessageVideoAgentBinding
    ) : MessageVideoViewHolder(binding.rootView, binding.content, false) {

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindAgent(chatItem, binding.agent)
        }
    }

    internal inner class MessageAudioAgentViewHolder(
        private val binding: MessageAudioAgentBinding
    ) : MessageAudioViewHolder(binding.rootView, binding.content, false) {

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindAgent(chatItem, binding.agent)
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

        /*private fun isSameDay(calendarA: Calendar?, calendarB: Calendar): Boolean {
            return calendarA?.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR) &&
                    calendarA.get(Calendar.DAY_OF_YEAR) == calendarB.get(Calendar.DAY_OF_YEAR)
        }*/
    }

    internal class MessageTextBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)
        val rvButtons: RecyclerView = rootView.findViewById(R.id.rv_buttons)
        val lFeedback: ViewGroup = rootView.findViewById(R.id.l_feedback)
        val tvText: TextView = rootView.findViewById(R.id.tv_text)
        val lContent: ViewGroup = rootView.findViewById(R.id.l_content)
        val ivLike: ImageView = rootView.findViewById(R.id.iv_like)
        val ivDislike: ImageView = rootView.findViewById(R.id.iv_dislike)
    }

    internal class MessageTextClientBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageTextBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val client = ClientBinding(rootView, defaultStyleId)
    }

    internal class MessageTextAgentBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageTextBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val agent = AgentBinding(rootView, defaultStyleId)
    }

    internal class MessageAgentNameBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvName: TextView = rootView.findViewById(R.id.tv_name)
    }

    internal class MessageFileBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)
        val tvFileName: TextView = rootView.findViewById(R.id.tv_file_name)
        val tvFileSize: TextView = rootView.findViewById(R.id.tv_file_size)
        val tvExtension: TextView = rootView.findViewById(R.id.tv_extension)
    }

    internal class MessageFileClientBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageFileBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val client = ClientBinding(rootView, defaultStyleId)
    }

    internal class MessageFileAgentBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageFileBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val agent = AgentBinding(rootView, defaultStyleId)
    }

    internal class MessageImageBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)
        val ivPreview: RoundedImageView = rootView.findViewById(R.id.iv_preview)
        val ivError: ImageView = rootView.findViewById(R.id.iv_error)
        val pbLoading: ProgressBar = rootView.findViewById(R.id.pb_loading)
    }

    internal class MessageVideoBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)

        val lVideo: ViewGroup = rootView.findViewById(R.id.l_video)
        val lStub: ViewGroup = rootView.findViewById(R.id.l_stub)
        val ivPlay: ImageView = rootView.findViewById(R.id.iv_play)
        val ivPreview: ImageView = rootView.findViewById(R.id.iv_preview)
    }

    internal class MessageAudioBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val tvTime: TextView = rootView.findViewById(R.id.tv_time)
        val lAudio: ViewGroup = rootView.findViewById(R.id.l_audio)
        val lStub: ViewGroup = rootView.findViewById(R.id.stub)
        val stubProgress: View = rootView.findViewById(R.id.stub_progress)
        val stubScrubber: View = rootView.findViewById(R.id.stub_scrubber)
        val exoPosition: TextView = rootView.findViewById(R.id.exo_position)
        val tvDownload: TextView = rootView.findViewById(R.id.tv_download)
        val ivExoPlay: ImageView = rootView.findViewById(R.id.exo_play)
        val ivExoPause: ImageView = rootView.findViewById(R.id.exo_pause)
    }

    internal class MessageImageClientBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageImageBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val client = ClientBinding(rootView, defaultStyleId)
    }

    internal class MessageVideoClientBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageVideoBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val client = ClientBinding(rootView, defaultStyleId)
    }

    internal class MessageAudioClientBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageAudioBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val client = ClientBinding(rootView, defaultStyleId)
    }

    internal class MessageImageAgentBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageImageBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val agent = AgentBinding(rootView, defaultStyleId)
    }

    internal class MessageVideoAgentBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageVideoBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val agent = AgentBinding(rootView, defaultStyleId)
    }

    internal class MessageAudioAgentBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val content = MessageAudioBinding(rootView.findViewById(R.id.content), defaultStyleId)
        val agent = AgentBinding(rootView, defaultStyleId)
    }

    internal class AgentBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val ivAvatar: ImageView = rootView.findViewById(R.id.iv_avatar)
        val tvName: TextView = rootView.findViewById(R.id.tv_name)
        val vEmpty: View = rootView.findViewById(R.id.v_empty)
    }

    internal class ClientBinding(rootView: View, defaultStyleId: Int) :
        UsedeskBinding(rootView, defaultStyleId) {
        val vEmpty: View = rootView.findViewById(R.id.v_empty)
        val ivSentFailed: ImageView = rootView.findViewById(R.id.iv_sent_failed)
    }
}