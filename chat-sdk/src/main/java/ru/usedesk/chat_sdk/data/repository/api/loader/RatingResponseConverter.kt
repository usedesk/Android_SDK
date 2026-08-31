package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse
import ru.usedesk.chat_sdk.entity.UsedeskRating

internal object RatingResponseConverter {

    private const val LEGACY_LIKE_ID = "1"
    private const val LEGACY_DISLIKE_ID = "3"
    private const val LEGACY_LIKE_DATA = "LIKE"
    private const val LEGACY_DISLIKE_DATA = "DISLIKE"
    private const val ICON_TYPE_REACTION = "reaction"
    private const val LABEL_LIKE = "like"
    private const val LABEL_DISLIKE = "dislike"

    fun convert(payload: SocketResponse.AddMessage.Message.Payload?): UsedeskRating? {
        if (payload == null) return null

        val rating = payload.buttonsCsi
            ?.takeIf { it.buttonType.equals("action", ignoreCase = true) }
            ?.let { buttonsCsi ->
                val items = buttonsCsi.data.orEmpty()
                val options = items.mapNotNull { item ->
                    item?.id?.takeIf { it.isNotBlank() }?.let { id ->
                        UsedeskRating.Option(
                            id = id,
                            label = item.label.orEmpty(),
                            reaction = item.label.toReaction()
                        )
                    }
                }
                options.takeIf {
                    it.isNotEmpty() &&
                            it.size == items.size &&
                            it.map(UsedeskRating.Option::id).distinct().size == it.size
                }?.let { validOptions ->
                    val selectedId = payload.userRating.toSelectedId(validOptions)
                    if (!payload.userRating.isNullOrBlank() && selectedId == null) {
                        null
                    } else {
                        UsedeskRating(
                            appearance = buttonsCsi.appearance(validOptions),
                            options = validOptions,
                            selectedId = selectedId
                        )
                    }
                }
            }
        if (rating != null) return rating

        val legacyFeedbackPresent = payload.buttons.orEmpty().any { button ->
            button?.data.equals("GOOD_CHAT", ignoreCase = true) ||
                    button?.data.equals("BAD_CHAT", ignoreCase = true) ||
                    button?.icon.equals(LABEL_LIKE, ignoreCase = true) ||
                    button?.icon.equals(LABEL_DISLIKE, ignoreCase = true)
        }
        val legacyRatingPresent = payload.userRating.isLegacyRating()
        if (!legacyFeedbackPresent && !legacyRatingPresent) return null

        // Preserve the legacy UI order.
        val options = listOf(
            UsedeskRating.Option(
                id = LEGACY_DISLIKE_ID,
                label = LABEL_DISLIKE,
                data = LEGACY_DISLIKE_DATA,
                reaction = UsedeskRating.Option.Reaction.DISLIKE
            ),
            UsedeskRating.Option(
                id = LEGACY_LIKE_ID,
                label = LABEL_LIKE,
                data = LEGACY_LIKE_DATA,
                reaction = UsedeskRating.Option.Reaction.LIKE
            )
        )
        val selectedId = payload.userRating.toSelectedId(options)
        if (!payload.userRating.isNullOrBlank() && selectedId == null) {
            // Do not reopen an already rated message.
            return null
        }
        return UsedeskRating(
            appearance = UsedeskRating.Appearance.REACTIONS,
            options = options,
            selectedId = selectedId
        )
    }

    private fun String?.toSelectedId(options: List<UsedeskRating.Option>): String? {
        val rating = this?.trim()?.takeIf(String::isNotEmpty) ?: return null
        options.firstOrNull { it.id == rating }?.let { return it.id }
        val normalizedLabel = when {
            rating.equals("LIKE", ignoreCase = true) ||
                    rating.equals("GOOD_CHAT", ignoreCase = true) -> LABEL_LIKE
            rating.equals("DISLIKE", ignoreCase = true) ||
                    rating.equals("BAD_CHAT", ignoreCase = true) -> LABEL_DISLIKE
            else -> rating
        }
        return options.firstOrNull { it.label.equals(normalizedLabel, ignoreCase = true) }?.id
    }

    private fun String?.isLegacyRating(): Boolean =
        equals("LIKE", ignoreCase = true) ||
                equals("DISLIKE", ignoreCase = true) ||
                equals("GOOD_CHAT", ignoreCase = true) ||
                equals("BAD_CHAT", ignoreCase = true)

    private fun SocketResponse.AddMessage.Message.Payload.ButtonsCsi.appearance(
        options: List<UsedeskRating.Option>
    ): UsedeskRating.Appearance {
        val reactions = options.map(UsedeskRating.Option::reaction).toSet()
        return if (
            iconType.equals(ICON_TYPE_REACTION, ignoreCase = true) &&
            options.size == 2 &&
            reactions == REACTIONS
        ) {
            UsedeskRating.Appearance.REACTIONS
        } else {
            UsedeskRating.Appearance.STARS
        }
    }

    private fun String?.toReaction(): UsedeskRating.Option.Reaction? = when {
        equals(LABEL_LIKE, ignoreCase = true) -> UsedeskRating.Option.Reaction.LIKE
        equals(LABEL_DISLIKE, ignoreCase = true) -> UsedeskRating.Option.Reaction.DISLIKE
        else -> null
    }

    private val REACTIONS = setOf(
        UsedeskRating.Option.Reaction.LIKE,
        UsedeskRating.Option.Reaction.DISLIKE
    )
}
