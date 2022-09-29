package ru.usedesk.chat_sdk.data.repository.api.loader

import android.util.Patterns
import ru.usedesk.chat_sdk.data.repository._extra.Converter
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.utils.UsedeskDateUtil.Companion.getLocalCalendar
import java.util.regex.Pattern
import javax.inject.Inject

internal class MessageResponseConverter @Inject constructor() :
    Converter<MessageResponse.Message?, List<UsedeskMessage>>() {

    private val emailRegex = Patterns.EMAIL_ADDRESS.toRegex()
    private val phoneRegex = Patterns.PHONE.toRegex()
    private val urlRegex = Patterns.WEB_URL.toRegex()
    private val mdUrlRegex = Pattern.compile(
        "\\[[^\\[\\]\\(\\)]+\\]\\(${urlRegex.pattern}/?\\)"
    ).toRegex()


    fun convertText(text: String) = try {
        text.trim('\n', '\r', ' ', '\u200B')
            .split('\n')
            .joinToString("\n") {
                it.trim('\r', ' ', '\u200B')
                    .convertMarkdownUrls()
                    .convertMarkdownText()
            }
            .replace("\n\n", "\n")
            .replace("\n", "<br>")
    } catch (e: Exception) {
        e.printStackTrace()
        text
    }

    override fun convert(from: MessageResponse.Message?): List<UsedeskMessage> {
        return convertOrNull {
            val fromClient = when (from!!.type) {
                MessageResponse.TYPE_CLIENT_TO_OPERATOR,
                MessageResponse.TYPE_CLIENT_TO_BOT -> true
                MessageResponse.TYPE_OPERATOR_TO_CLIENT,
                MessageResponse.TYPE_BOT_TO_CLIENT -> false
                else -> null
            }!!

            val createdAt = from.createdAt!!

            val messageDate = try {
                getLocalCalendar("yyyy-MM-dd'T'HH:mm:ss'Z'", createdAt)
            } catch (e: Exception) {
                getLocalCalendar("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", createdAt)
            }

            val id = from.id!!
            val localId = from.payload?.messageId ?: id
            val name = from.name ?: ""
            val avatar = from.payload?.avatar ?: ""

            val fileMessages = mutableListOf<UsedeskMessageFile>()

            convertOrNull {
                val file = UsedeskFile.create(
                    from.file!!.content!!,
                    from.file!!.type,
                    from.file!!.size!!,
                    from.file!!.name!!
                )

                when {
                    fromClient -> when {
                        file.isImage() -> UsedeskMessageClientImage(
                            id,
                            messageDate,
                            file,
                            UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                            localId
                        )
                        file.isVideo() -> UsedeskMessageClientVideo(
                            id,
                            messageDate,
                            file,
                            UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                            localId
                        )
                        file.isAudio() -> UsedeskMessageClientAudio(
                            id,
                            messageDate,
                            file,
                            UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                            localId
                        )
                        else -> UsedeskMessageClientFile(
                            id,
                            messageDate,
                            file,
                            UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                            localId
                        )
                    }
                    else -> when {
                        file.isImage() -> UsedeskMessageAgentImage(
                            id,
                            messageDate,
                            file,
                            name,
                            avatar
                        )
                        file.isVideo() -> UsedeskMessageAgentVideo(
                            id,
                            messageDate,
                            file,
                            name,
                            avatar
                        )
                        file.isAudio() -> UsedeskMessageAgentAudio(
                            id,
                            messageDate,
                            file,
                            name,
                            avatar
                        )
                        else -> UsedeskMessageAgentFile(
                            id,
                            messageDate,
                            file,
                            name,
                            avatar
                        )
                    }
                }
            }?.let(fileMessages::add)

            val textMessage = convertOrNull {
                if (from.text?.isNotEmpty() == true) {
                    val buttons: List<UsedeskMessageButton>
                    val feedbackNeeded: Boolean
                    val feedback: UsedeskFeedback?
                    if (!fromClient) {
                        buttons = getButtons(from.text!!)
                        feedback = when (from.payload?.userRating) {
                            "LIKE" -> UsedeskFeedback.LIKE
                            "DISLIKE" -> UsedeskFeedback.DISLIKE
                            else -> null
                        }
                        feedbackNeeded = feedback == null && from.payload?.buttons?.any {
                            it?.data == "GOOD_CHAT" ||
                                    it?.data == "BAD_CHAT" ||
                                    it?.icon == "like" ||
                                    it?.icon == "dislike"
                        } ?: false
                    } else {
                        buttons = listOf()
                        feedbackNeeded = false
                        feedback = null
                    }

                    val imageRegexp = "!\\[[^]]*]\\((.*?)\\s*(\"(?:.*[^\"])\")?\\s*\\)".toRegex()

                    var convertedText = from.text!!

                    var matcher = imageRegexp.find(convertedText)
                    while (matcher != null) {
                        val section = matcher.value.removePrefix("![")
                            .removeSuffix(")")
                        val fileName = section.substringBefore("](")
                        val fileUrl = section.substringAfter("](")
                        convertedText = convertedText.replace(matcher.value, "")
                        matcher = imageRegexp.find(convertedText)

                        val file = UsedeskFile.create(
                            fileUrl,
                            "image/*",
                            "0",
                            fileName
                        )
                        fileMessages.add(
                            when {
                                fromClient -> UsedeskMessageClientImage(
                                    id,
                                    messageDate,
                                    file,
                                    UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                                    localId
                                )
                                else -> UsedeskMessageAgentImage(
                                    id,
                                    messageDate,
                                    file,
                                    name,
                                    avatar
                                )
                            }
                        )
                    }

                    convertedText = convertedText.replace(
                        "<strong data-verified=\"redactor\" data-redactor-tag=\"strong\">",
                        "<b>"
                    ).replace("</strong>", "</b>")
                        .replace("<em data-verified=\"redactor\" data-redactor-tag=\"em\">", "<i>")
                        .replace("</em>", "</i>")
                        .replace("</p>", "")
                        .removePrefix("<p>")

                    buttons.forEach {
                        val show: String
                        val replaceBy: String
                        if (it.isShow) {
                            show = "show"
                            replaceBy = it.text
                        } else {
                            show = "noshow"
                            replaceBy = ""
                        }
                        val buttonRaw = "{{button:${it.text};${it.url};${it.type};$show}}"
                        convertedText = convertedText.replaceFirst(buttonRaw, replaceBy)
                    }
                    convertedText = convertText(convertedText)

                    when {
                        convertedText.isEmpty() && buttons.isEmpty() -> null
                        fromClient -> UsedeskMessageClientText(
                            id,
                            messageDate,
                            from.text!!,
                            convertedText,
                            UsedeskMessageClient.Status.SUCCESSFULLY_SENT,
                            localId
                        )
                        else -> UsedeskMessageAgentText(
                            id,
                            messageDate,
                            from.text!!,
                            convertedText,
                            buttons,
                            feedbackNeeded,
                            feedback,
                            name,
                            avatar
                        )
                    }
                } else {
                    null
                }
            }

            (listOf(textMessage) + fileMessages).filterNotNull()
        } ?: listOf()
    }

    private fun String.convertMarkdownText(): String {
        val builder = StringBuilder()
        var i = 0
        var boldOpen = true
        var italicOpen = true
        while (i < this.length) {
            builder.append(
                when (this[i]) {
                    '*' -> when {
                        this.getOrNull(i + 1) == '*' -> {
                            i++
                            boldOpen = !boldOpen
                            if (boldOpen) "</b>"
                            else "<b>"
                        }
                        else -> {
                            italicOpen = !italicOpen
                            if (italicOpen) "</i>"
                            else "<i>"
                        }
                    }
                    '\n' -> "<br>"
                    else -> this[i]
                }
            )
            i++
        }
        return builder.toString()
    }

    private fun Regex.findAll(
        text: String,
        includedRanges: List<IntRange>
    ) = includedRanges.flatMap { part ->
        this.findAll(text.substring(part))
            .map { (it.range.first + part.first)..(it.range.last + part.first) }
    }

    private fun String.getExcludeRanges(
        includedRanges: List<IntRange>
    ): List<IntRange> {
        val ranges = includedRanges.sortedBy { it.first }
        return (sequenceOf(
            0 until (ranges.firstOrNull()?.first ?: length),
            (ranges.lastOrNull()?.last?.inc() ?: 0) until length
        ) + ranges.indices.mapNotNull { i ->
            if (i < ranges.size - 1) {
                ranges[i].last + 1 until ranges[i + 1].first
            } else {
                null
            }
        }.asSequence())
            .filter { it.first <= it.last && it.first in this.indices && it.last in this.indices }
            .toSet()
            .toList()
    }

    private fun String.convertMarkdownUrls(): String {
        val withMdUrlsRanges = mdUrlRegex.findAll(this, listOf(this.indices))

        val noMdUrlsRanges = getExcludeRanges(withMdUrlsRanges)

        val emails = emailRegex.findAll(this, noMdUrlsRanges)

        val withEmailsRanges = withMdUrlsRanges + emails
        val noEmailsRanges = getExcludeRanges(withEmailsRanges)

        val urls = urlRegex.findAll(this, noEmailsRanges)

        val withUrlsRanges = withEmailsRanges + urls
        val noUrlsRanges = getExcludeRanges(withUrlsRanges)

        val phones = phoneRegex.findAll(this, noUrlsRanges)

        val withPhonesRanges = withUrlsRanges + phones
        val noPhones = getExcludeRanges(withPhonesRanges)

        val builder = StringBuilder()

        (withPhonesRanges + noPhones).toSet()
            .sortedBy { it.first }
            .forEach {
                val part = this.substring(it)
                builder.append(when (it) {
                    in withMdUrlsRanges -> {
                        val parts = part.trim('[', ')')
                            .split("](")
                        val url = parts[1]
                        val title = parts[0].ifEmpty { url }
                        makeHtmlUrl(url, title)
                    }
                    in urls -> makeHtmlUrl(part)
                    in emails -> makeHtmlUrl("mailto:$part", part)
                    in phones -> makeHtmlUrl("tel:$part", part)
                    else -> part
                })
            }

        return builder.toString()
    }

    private fun makeHtmlUrl(url: String, title: String = url) = "<a href=\"$url\">$title</a>"

    private fun getButtons(messageText: String): List<UsedeskMessageButton> {
        val messageButtons = mutableListOf<UsedeskMessageButton>()

        var start = 0
        while (messageText.indexOf("{{button:", start).apply { start = this } >= 0) {
            val end = messageText.indexOf("}}", start)

            val buttonText = messageText.substring(start, end + 2)
            val messageButton = getButton(buttonText)
            if (messageButton != null) {
                messageButtons.add(messageButton)
            }
            start++
        }

        return messageButtons
    }

    private fun getButton(messageText: String): UsedeskMessageButton? {
        val sections = messageText.replace("{{button:", "")
            .replace("}}", "")
            .split(";")
        return when (sections.size) {
            4 -> {
                val text = sections[0]
                val url = sections[1]
                val type = sections[2]
                val isShow = sections[3] == "show"
                UsedeskMessageButton(text, url, type, isShow)
            }
            else -> null
        }
    }
}