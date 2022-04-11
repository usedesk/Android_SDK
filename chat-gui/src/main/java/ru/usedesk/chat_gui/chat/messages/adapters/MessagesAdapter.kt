package ru.usedesk.chat_gui.chat.messages.adapters

import android.graphics.Rect
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
import ru.usedesk.chat_gui.R
import ru.usedesk.chat_gui.chat.MediaPlayerAdapter
import ru.usedesk.chat_gui.chat.messages.DateBinding
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel
import ru.usedesk.chat_gui.chat.messages.MessagesViewModel.*
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.chat_sdk.entity.UsedeskMessage.Type
import ru.usedesk.common_gui.*
import java.text.SimpleDateFormat
import java.util.*


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
    private val layoutManager = LinearLayoutManager(recyclerView.context)

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
    }

    fun updateFloatingDate() {
        if (items.isNotEmpty()) {
            dateBinding.tvDate.post {
                val firstIndex = layoutManager.findFirstVisibleItemPosition()
                if (firstIndex >= 0) {
                    val lastIndex = layoutManager.findLastVisibleItemPosition()

                    val itemsSequence = items.asSequence()
                    val visibleItems = itemsSequence.drop(firstIndex)
                        .take(lastIndex - firstIndex)
                    val visibleDateItems = visibleItems.filterIsInstance<ChatDate>()
                    val topDateItem = visibleDateItems.firstOrNull()
                    val topDateIndex = if (topDateItem != null) {
                        items.indexOf(topDateItem)
                    } else {
                        -1
                    }

                    visibleDateItems.map {
                        items.indexOf(it)
                    }.mapNotNull {
                        recyclerView.findViewHolderForAdapterPosition(it)
                    }.filterIsInstance<DateViewHolder>()
                        .forEach {
                            it.binding.rootView.visibility = View.VISIBLE
                        }

                    val floatingDateRect = Rect()
                    (dateBinding.rootView.parent as View).getGlobalVisibleRect(floatingDateRect)
                    floatingDateRect.bottom =
                        floatingDateRect.top + dateBinding.rootView.measuredHeight

                    dateBinding.rootView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        updateMargins(
                            top = 0
                        )
                    }
                    recyclerView.apply {
                        val topDateViewHolder =
                            findViewHolderForAdapterPosition(topDateIndex) as? DateViewHolder

                        if (topDateViewHolder != null) {
                            val topDateRect = Rect().also {
                                topDateViewHolder.binding.rootView.getGlobalVisibleRect(it)
                            }
                            if (topDateRect.bottom > floatingDateRect.bottom &&
                                topDateRect.top < floatingDateRect.bottom
                            ) {
                                val dif = floatingDateRect.bottom - topDateRect.top
                                dateBinding.rootView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                    updateMargins(
                                        top = -dif
                                    )
                                }
                            } else if (topDateRect.bottom <= floatingDateRect.bottom) {
                                topDateViewHolder.binding.rootView.visibility = View.INVISIBLE
                                dateBinding.tvDate.text = topDateViewHolder.binding.tvDate.text
                                dateBinding.rootView.visibility = View.VISIBLE
                                return@post
                            }
                        }

                        val notVisibleDateItem = itemsSequence
                            .take(firstIndex)
                            .filterIsInstance<ChatDate>()
                            .lastOrNull()

                        if (notVisibleDateItem != null) {
                            dateBinding.tvDate.text = getDateText(notVisibleDateItem)
                            dateBinding.rootView.visibility = View.VISIBLE
                        } else {
                            dateBinding.rootView.visibility = View.INVISIBLE
                        }
                    }
                } else {
                    dateBinding.rootView.visibility = View.INVISIBLE
                }
            }
        }
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
                if (old is ClientMessage && new is ClientMessage) {
                    println()
                }
                val result = when (new) {
                    is ChatDate -> old is ChatDate &&
                            old.calendar.timeInMillis == new.calendar.timeInMillis
                    is ChatMessage -> old is ChatMessage && old.isIdEquals(new)
                }
                if (!result && new is ClientMessage && old is ClientMessage) {
                    val a = old.isIdEquals(new)
                    println()
                }
                return result
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = oldItems[oldItemPosition]
                val new = items[newItemPosition]
                val result = when (new) {
                    is ChatDate -> true
                    is ChatMessage -> when {
                        old !is ChatMessage -> false
                        (new.isLastOfGroup == old.isLastOfGroup) -> false
                        (new.message as? UsedeskMessageText)?.text !=
                                (old.message as? UsedeskMessageText)?.text -> false
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
            recyclerView.scrollToPosition(items.size - 1)
        } else {
            updateToBottomButton()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            R.layout.usedesk_item_chat_date -> {
                DateViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_date,
                    R.style.Usedesk_Chat_Date
                ) { rootView, defaultStyleId ->
                    DateBinding(rootView, defaultStyleId)
                })
            }
            R.layout.usedesk_item_chat_message_text_agent -> {
                MessageTextAgentViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_message_text_agent,
                    R.style.Usedesk_Chat_Message_Text_Agent
                ) { rootView, defaultStyleId ->
                    MessageTextAgentBinding(rootView, defaultStyleId)
                })
            }
            R.layout.usedesk_item_chat_message_file_agent -> {
                MessageFileAgentViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_message_file_agent,
                    R.style.Usedesk_Chat_Message_File_Agent
                ) { rootView, defaultStyleId ->
                    MessageFileAgentBinding(rootView, defaultStyleId)
                })
            }
            R.layout.usedesk_item_chat_message_image_agent -> {
                MessageImageAgentViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_message_image_agent,
                    R.style.Usedesk_Chat_Message_Image_Agent
                ) { rootView, defaultStyleId ->
                    MessageImageAgentBinding(rootView, defaultStyleId)
                })
            }
            R.layout.usedesk_item_chat_message_video_agent -> {
                MessageVideoAgentViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_message_video_agent,
                    R.style.Usedesk_Chat_Message_Video_Agent
                ) { rootView, defaultStyleId ->
                    MessageVideoAgentBinding(rootView, defaultStyleId)
                })
            }
            R.layout.usedesk_item_chat_message_audio_agent -> {
                MessageAudioAgentViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_message_audio_agent,
                    R.style.Usedesk_Chat_Message_Audio_Agent
                ) { rootView, defaultStyleId ->
                    MessageAudioAgentBinding(rootView, defaultStyleId)
                })
            }
            R.layout.usedesk_item_chat_message_text_client -> {
                MessageTextClientViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_message_text_client,
                    R.style.Usedesk_Chat_Message_Text_Client
                ) { rootView, defaultStyleId ->
                    MessageTextClientBinding(rootView, defaultStyleId)
                })
            }
            R.layout.usedesk_item_chat_message_file_client -> {
                MessageFileClientViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_message_file_client,
                    R.style.Usedesk_Chat_Message_File_Client
                ) { rootView, defaultStyleId ->
                    MessageFileClientBinding(rootView, defaultStyleId)
                })
            }
            R.layout.usedesk_item_chat_message_image_client -> {
                MessageImageClientViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_message_image_client,
                    R.style.Usedesk_Chat_Message_Image_Client
                ) { rootView, defaultStyleId ->
                    MessageImageClientBinding(rootView, defaultStyleId)
                })
            }
            R.layout.usedesk_item_chat_message_video_client -> {
                MessageVideoClientViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_message_video_client,
                    R.style.Usedesk_Chat_Message_Video_Client
                ) { rootView, defaultStyleId ->
                    MessageVideoClientBinding(rootView, defaultStyleId)
                })
            }
            R.layout.usedesk_item_chat_message_audio_client -> {
                MessageAudioClientViewHolder(inflateItem(
                    parent,
                    R.layout.usedesk_item_chat_message_audio_client,
                    R.style.Usedesk_Chat_Message_Audio_Client
                ) { rootView, defaultStyleId ->
                    MessageAudioClientBinding(rootView, defaultStyleId)
                })
            }
            else -> {
                throw RuntimeException("Unknown view type:$viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ChatDate -> R.layout.usedesk_item_chat_date
            is ChatMessage -> when (item.message.type) {
                Type.TYPE_AGENT_TEXT -> R.layout.usedesk_item_chat_message_text_agent
                Type.TYPE_AGENT_IMAGE -> R.layout.usedesk_item_chat_message_image_agent
                Type.TYPE_AGENT_VIDEO -> R.layout.usedesk_item_chat_message_video_agent
                Type.TYPE_AGENT_AUDIO -> R.layout.usedesk_item_chat_message_audio_agent
                Type.TYPE_AGENT_FILE -> R.layout.usedesk_item_chat_message_file_agent
                Type.TYPE_CLIENT_TEXT -> R.layout.usedesk_item_chat_message_text_client
                Type.TYPE_CLIENT_IMAGE -> R.layout.usedesk_item_chat_message_image_client
                Type.TYPE_CLIENT_VIDEO -> R.layout.usedesk_item_chat_message_video_client
                Type.TYPE_CLIENT_AUDIO -> R.layout.usedesk_item_chat_message_audio_client
                Type.TYPE_CLIENT_FILE -> R.layout.usedesk_item_chat_message_file_client
            }
        }
    }

    fun scrollToBottom() {
        recyclerView.smoothScrollToPosition(items.size - 1)
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
        private val styleValues: UsedeskResourceManager.StyleValues
    ) : BaseViewHolder(itemView) {

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

            agentBinding.ivAvatar.visibility = if (chatItem.showAvatar) {
                visibleState
            } else {
                invisibleState
            }
            agentBinding.vEmpty.visibility = View.INVISIBLE
        }

        fun bindClient(
            chatItem: ChatItem,
            clientBinding: ClientBinding
        ) {
            val clientMessage = (chatItem as ChatMessage).message as UsedeskMessageClient
            val timeStyleValues = styleValues.getStyleValues(R.attr.usedesk_chat_message_time_text)
            val statusDrawableId =
                if (clientMessage.status == UsedeskMessageClient.Status.SUCCESSFULLY_SENT) {
                    timeStyleValues.getId(R.attr.usedesk_drawable_2)
                } else {
                    timeStyleValues.getId(R.attr.usedesk_drawable_1)
                }

            tvTime.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                statusDrawableId,
                0
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
        private val binding: MessageTextBinding
    ) : MessageViewHolder(itemView, binding.tvTime, binding.styleValues) {

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)

            binding.lFeedback.visibility = View.GONE

            val messageText = (chatItem as ChatMessage).message as UsedeskMessageText

            binding.tvText.text = Html.fromHtml(messageText.text.replace("\n", "<br>"))
            binding.tvText.visibility = View.VISIBLE
        }
    }

    internal abstract inner class MessageFileViewHolder(
        itemView: View,
        private val binding: MessageFileBinding
    ) : MessageViewHolder(itemView, binding.tvTime, binding.styleValues) {

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
        private val binding: MessageImageBinding
    ) : MessageViewHolder(itemView, binding.tvTime, binding.styleValues) {

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
                        binding.ivPreview.setOnClickListener {
                            onFileClick(messageFile.file)
                        }
                    },
                    onError = {
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
        private val binding: MessageVideoBinding
    ) : MessageViewHolder(itemView, binding.tvTime, binding.styleValues) {

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
        private val binding: MessageAudioBinding
    ) : MessageViewHolder(itemView, binding.tvTime, binding.styleValues) {

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
    ) : MessageTextViewHolder(binding.rootView, binding.content) {
        init {
            if (adaptiveTextMessageTimePadding) {
                applyAdaptivePadding(
                    binding.content.tvTime,
                    binding.content.lContent,
                    true
                )
            }
        }

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindClient(chatItem, binding.client)
        }
    }

    internal inner class MessageFileClientViewHolder(
        private val binding: MessageFileClientBinding
    ) : MessageFileViewHolder(binding.rootView, binding.content) {
        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindClient(chatItem, binding.client)
        }
    }

    internal inner class MessageImageClientViewHolder(
        private val binding: MessageImageClientBinding
    ) : MessageImageViewHolder(binding.rootView, binding.content) {
        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindClient(chatItem, binding.client)
        }
    }

    internal inner class MessageVideoClientViewHolder(
        private val binding: MessageVideoClientBinding
    ) : MessageVideoViewHolder(binding.rootView, binding.content) {
        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindClient(chatItem, binding.client)
        }
    }

    private fun applyAdaptivePadding(
        tvTime: TextView,
        content: View,
        withIcon: Boolean = false
    ) {
        content.apply {
            val bounds = Rect()
            tvTime.paint.getTextBounds(
                timeFormatText,
                0,
                timeFormatText.length,
                bounds
            )
            (layoutParams as ViewGroup.MarginLayoutParams).updateMargins(
                right = tvTime.marginStart +
                        tvTime.marginEnd +
                        marginEnd +
                        bounds.width() +
                        if (withIcon) {
                            bounds.height()
                        } else {
                            0
                        }
            )
        }
    }

    internal inner class MessageAudioClientViewHolder(
        private val binding: MessageAudioClientBinding
    ) : MessageAudioViewHolder(binding.rootView, binding.content) {
        init {
            applyAdaptivePadding(
                binding.content.tvTime,
                binding.content.tvDownload,
                true
            )
        }

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
        }
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
    ) : MessageTextViewHolder(binding.rootView, binding.content) {

        private val goodStyleValues = binding.styleValues
            .getStyleValues(R.attr.usedesk_chat_message_feedback_good_image)

        private val badStyleValues = binding.styleValues
            .getStyleValues(R.attr.usedesk_chat_message_feedback_bad_image)

        private val thanksText = binding.styleValues
            .getStyleValues(R.attr.usedesk_chat_message_text_message_text)
            .getString(R.attr.usedesk_text_1)

        init {
            if (adaptiveTextMessageTimePadding) {
                applyAdaptivePadding(
                    binding.content.tvTime,
                    binding.content.lContent
                )
            }
        }

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

    internal inner class MessageFileAgentViewHolder(
        private val binding: MessageFileAgentBinding
    ) : MessageFileViewHolder(binding.rootView, binding.content) {

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindAgent(chatItem, binding.agent)
        }
    }

    internal inner class MessageImageAgentViewHolder(
        private val binding: MessageImageAgentBinding
    ) : MessageImageViewHolder(binding.rootView, binding.content) {

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindAgent(chatItem, binding.agent)
        }
    }

    internal inner class MessageVideoAgentViewHolder(
        private val binding: MessageVideoAgentBinding
    ) : MessageVideoViewHolder(binding.rootView, binding.content) {

        override fun bind(chatItem: ChatItem) {
            super.bind(chatItem)
            bindAgent(chatItem, binding.agent)
        }
    }

    internal inner class MessageAudioAgentViewHolder(
        private val binding: MessageAudioAgentBinding
    ) : MessageAudioViewHolder(binding.rootView, binding.content) {

        init {
            applyAdaptivePadding(
                binding.content.tvTime,
                binding.content.tvDownload
            )
        }

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