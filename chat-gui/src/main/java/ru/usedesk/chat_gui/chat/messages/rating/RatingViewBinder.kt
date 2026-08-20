package ru.usedesk.chat_gui.chat.messages.rating

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import ru.usedesk.chat_sdk.entity.UsedeskRating
import ru.usedesk.common_gui.UsedeskResourceManager
import ru.usedesk.chat_gui.R as chatR
import ru.usedesk.common_gui.R as commonR

internal class RatingViewBinder(
    private val root: ViewGroup,
    private val options: ViewGroup,
    private val like: ImageView,
    private val dislike: ImageView,
    styleValues: UsedeskResourceManager.StyleValues
) {

    private val goodStyle = styleValues
        .getStyleValues(chatR.attr.usedesk_chat_message_feedback_good_image)
    private val badStyle = styleValues
        .getStyleValues(chatR.attr.usedesk_chat_message_feedback_bad_image)
    private val starStyle = styleValues
        .getStyleValues(chatR.attr.usedesk_chat_message_feedback_star_image)
    private val goodAtStart = goodStyle
        .getInt(android.R.attr.layout_gravity) in arrayOf(Gravity.START, Gravity.LEFT)
    private val reactionWidth = root.layoutParams.width
    private val inflater = LayoutInflater.from(options.context)

    fun bind(rating: UsedeskRating, onSelect: (Long) -> Unit) {
        root.visibility = View.VISIBLE
        when (rating.appearance) {
            UsedeskRating.Appearance.REACTIONS -> bindReactions(rating, onSelect)
            UsedeskRating.Appearance.STARS -> bindStars(rating, onSelect)
        }
    }

    fun clear() {
        hideReactions()
        options.removeAllViews()
        options.visibility = View.GONE
        root.visibility = View.GONE
    }

    private fun bindReactions(rating: UsedeskRating, onSelect: (Long) -> Unit) {
        root.updateLayoutParams { width = reactionWidth }
        options.removeAllViews()
        options.visibility = View.GONE

        val likeOption = rating.option(UsedeskRating.Option.Reaction.LIKE)
        val dislikeOption = rating.option(UsedeskRating.Option.Reaction.DISLIKE)
        val selectedId = rating.selectedId

        like.visibility = View.VISIBLE
        dislike.visibility = View.VISIBLE
        if (selectedId == null) {
            enableReaction(
                main = like,
                secondary = dislike,
                atStart = goodAtStart,
                image = goodStyle.getId(commonR.attr.usedesk_drawable_1),
                selectedImage = goodStyle.getId(commonR.attr.usedesk_drawable_2)
            ) { onSelect(likeOption.id) }
            enableReaction(
                main = dislike,
                secondary = like,
                atStart = !goodAtStart,
                image = badStyle.getId(commonR.attr.usedesk_drawable_1),
                selectedImage = badStyle.getId(commonR.attr.usedesk_drawable_2)
            ) { onSelect(dislikeOption.id) }
        } else {
            showSelectedReaction(
                view = like,
                image = goodStyle.getId(commonR.attr.usedesk_drawable_2),
                visible = selectedId == likeOption.id
            )
            showSelectedReaction(
                view = dislike,
                image = badStyle.getId(commonR.attr.usedesk_drawable_2),
                visible = selectedId == dislikeOption.id
            )
        }
    }

    private fun bindStars(rating: UsedeskRating, onSelect: (Long) -> Unit) {
        hideReactions()
        root.updateLayoutParams { width = ViewGroup.LayoutParams.WRAP_CONTENT }
        options.visibility = View.VISIBLE
        options.removeAllViews()

        val selectedIndex = rating.options.indexOfFirst { it.id == rating.selectedId }
        rating.options.forEachIndexed { index, option ->
            val selected = selectedIndex >= 0 && index <= selectedIndex
            (inflater.inflate(
                chatR.layout.usedesk_item_chat_rating_star,
                options,
                false
            ) as ImageView).apply {
                setImageResource(
                    starStyle.getId(
                        if (selected) commonR.attr.usedesk_drawable_2
                        else commonR.attr.usedesk_drawable_1
                    )
                )
                contentDescription = option.label
                isEnabled = rating.selectedId == null
                isClickable = isEnabled
                isFocusable = isEnabled
                if (isEnabled) {
                    setOnClickListener {
                        bindStars(rating.copy(selectedId = option.id), onSelect)
                        onSelect(option.id)
                    }
                }
                options.addView(this)
            }
        }
    }

    private fun showSelectedReaction(view: ImageView, image: Int, visible: Boolean) {
        view.apply {
            animate().cancel()
            visibility = View.VISIBLE
            setImageResource(image)
            post { x = root.width / 4.0f }
            alpha = if (visible) 1.0f else 0.0f
            scaleX = 1.0f
            scaleY = 1.0f
            isEnabled = false
            isClickable = false
            setOnClickListener(null)
        }
    }

    private fun enableReaction(
        main: ImageView,
        secondary: ImageView,
        atStart: Boolean,
        image: Int,
        selectedImage: Int,
        onClick: () -> Unit
    ) {
        main.apply {
            animate().cancel()
            visibility = View.VISIBLE
            post { x = if (atStart) 0.0f else root.width / 2.0f }
            alpha = 1.0f
            scaleX = 1.0f
            scaleY = 1.0f
            isEnabled = true
            isClickable = true
            setImageResource(image)
            setOnClickListener {
                isEnabled = false
                isClickable = false
                setOnClickListener(null)
                secondary.apply {
                    isEnabled = false
                    isClickable = false
                    setOnClickListener(null)
                }
                setImageResource(selectedImage)
                onClick()
                animate().setDuration(500).x(root.width / 4.0f)
                secondary.animate()
                    .setDuration(500)
                    .alpha(0.0f)
                    .scaleX(0.5f)
                    .scaleY(0.5f)
            }
        }
    }

    private fun hideReactions() {
        listOf(like, dislike).forEach { view ->
            view.animate().cancel()
            view.visibility = View.GONE
            view.isEnabled = false
            view.isClickable = false
            view.setOnClickListener(null)
        }
    }

    private fun UsedeskRating.option(reaction: UsedeskRating.Option.Reaction) = options.first {
        it.reaction == reaction
    }
}
