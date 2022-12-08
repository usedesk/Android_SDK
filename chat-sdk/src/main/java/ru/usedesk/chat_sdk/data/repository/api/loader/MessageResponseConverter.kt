package ru.usedesk.chat_sdk.data.repository.api.loader

import android.util.Patterns
import ru.usedesk.chat_sdk.data.repository._extra.Converter
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.SocketResponse.AddMessage
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form
import ru.usedesk.chat_sdk.entity.UsedeskMessageAgentText.Form.Field.Text.Type
import ru.usedesk.common_sdk.api.UsedeskApiRepository.Companion.valueOrNull
import ru.usedesk.common_sdk.utils.UsedeskDateUtil.Companion.getLocalCalendar
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern
import javax.inject.Inject

internal class MessageResponseConverter @Inject constructor() :
    Converter<AddMessage.Message?, List<UsedeskMessage>> {

    private val fieldId = AtomicLong(-1L)

    private val emailRegex = Patterns.EMAIL_ADDRESS.toRegex()
    private val phoneRegex = Patterns.PHONE.toRegex()
    private val urlRegex = Patterns.WEB_URL.toRegex()
    private val mdUrlRegex = """\[[^\[\]\(\)]+\]\(${urlRegex.pattern}/?\)""".toRegex()
    private val badTagRegex1 =
        Pattern.compile("""<((${urlRegex.pattern})|(${emailRegex.pattern}))/>""")
    private val badTagRegex2 =
        Pattern.compile("""<((${urlRegex.pattern})|(${emailRegex.pattern}))>""")
    private val nextLineRegex = """\n{2,}""".toRegex()
    private val objectRegex = """\{\{$OBJECT_ANY\}\}""".toRegex()
    private val buttonRegex = """\{\{button:($OBJECT_PART){2}$OBJECT_ANY\}\}""".toRegex()
    private val fieldRegex = """\{\{form;($OBJECT_PART){1,2}$OBJECT_ANY\}\}""".toRegex()
    private val imageRegexp = """!\[[^]]*]\((.*?)\s*(\"(?:.*[^\"])\")?\s*\)""".toRegex()

    fun convertText(text: String): String = try {
        text.replace("""<strong data-verified="redactor" data-redactor-tag="strong">""", "<b>")
            .replace("</strong>", "</b>")
            .replace("""<em data-verified="redactor" data-redactor-tag="em">""", "<i>")
            .replace("</em>", "</i>")
            .replace("</p>", "")
            .removePrefix("<p>")
            .split('\n')
            .joinToString("\n") { line ->
                line.trim('\r', ' ', '\u200B')
                    .replace(badTagRegex1.toRegex()) { it.value.drop(1).dropLast(2) }
                    .replace(badTagRegex2.toRegex()) { it.value.drop(1).dropLast(1) }
                    .convertMarkdownUrls()
                    .convertMarkdownText()
            }
            .trim('\n')
            .replace(nextLineRegex, "\n\n")
            .replace("\n", "<br>")
    } catch (e: Exception) {
        e.printStackTrace()
        text
    }

    override fun convert(from: AddMessage.Message?): List<UsedeskMessage> = valueOrNull {
        val fromClient = when (from!!.type) {
            AddMessage.TYPE_CLIENT_TO_OPERATOR,
            AddMessage.TYPE_CLIENT_TO_BOT -> true
            AddMessage.TYPE_OPERATOR_TO_CLIENT,
            AddMessage.TYPE_BOT_TO_CLIENT -> false
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

        val fileMessage = valueOrNull {
            val file = UsedeskFile.create(
                from.file!!.content!!,
                from.file.type,
                from.file.size!!,
                from.file.name!!
            )

            when {
                fromClient -> when {
                    file.isImage() -> UsedeskMessageClientImage(
                        id,
                        messageDate,
                        file,
                        UsedeskMessageOwner.Client.Status.SUCCESSFULLY_SENT,
                        localId
                    )
                    file.isVideo() -> UsedeskMessageClientVideo(
                        id,
                        messageDate,
                        file,
                        UsedeskMessageOwner.Client.Status.SUCCESSFULLY_SENT,
                        localId
                    )
                    file.isAudio() -> UsedeskMessageClientAudio(
                        id,
                        messageDate,
                        file,
                        UsedeskMessageOwner.Client.Status.SUCCESSFULLY_SENT,
                        localId
                    )
                    else -> UsedeskMessageClientFile(
                        id,
                        messageDate,
                        file,
                        UsedeskMessageOwner.Client.Status.SUCCESSFULLY_SENT,
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
        }

        valueOrNull {
            val objects: List<MessageObject>
            val feedbackNeeded: Boolean
            val feedback: UsedeskFeedback?
            val text = from.text ?: ""
            if (!fromClient) {
                objects = text.toMessageObjects()
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
                objects = listOf(MessageObject.Text(text))
                feedbackNeeded = false
                feedback = null
            }

            val fields = objects.filterIsInstance<MessageObject.Field>()
                .map(MessageObject.Field::field)

            val fileMessages = objects.filterIsInstance<MessageObject.Image>()
                .map {
                    when {
                        fromClient -> UsedeskMessageClientImage(
                            id,
                            messageDate,
                            it.file,
                            UsedeskMessageOwner.Client.Status.SUCCESSFULLY_SENT,
                            localId
                        )
                        else -> UsedeskMessageAgentImage(
                            id,
                            messageDate,
                            it.file,
                            name,
                            avatar
                        )
                    }
                }

            val convertedText = convertText(
                objects.filterIsInstance<MessageObject.Text>()
                    .joinToString(separator = "", transform = MessageObject.Text::text)
            )

            listOf(
                when {
                    convertedText.isEmpty() && fields.isEmpty() -> null
                    fromClient -> UsedeskMessageClientText(
                        id,
                        messageDate,
                        text,
                        convertedText,
                        UsedeskMessageOwner.Client.Status.SUCCESSFULLY_SENT,
                        localId
                    )
                    else -> {
                        val formsLoaded = fields.all { it !is Form.Field.List }
                        val buttons = fields.filterIsInstance<Form.Button>()
                        val forms = fields.filter { it !is Form.Button }

                        UsedeskMessageAgentText(
                            id,
                            messageDate,
                            text,
                            convertedText,
                            name,
                            avatar,
                            feedbackNeeded,
                            feedback,
                            when {
                                forms.isNotEmpty() && formsLoaded -> buttons + forms + Form.Button()
                                else -> buttons + forms
                            },
                            formsLoaded = formsLoaded
                        )
                    }
                }
            ) + fileMessage + fileMessages
        }?.filterNotNull()
    } ?: listOf()

    private fun String.convertMarkdownText() = StringBuilder().also { builder ->
        var i = 0
        var boldOpen = true
        var italicOpen = true
        while (i < this.length) {
            builder.append(
                when (this[i]) {
                    '*' -> when (getOrNull(i + 1)) {
                        '*' -> {
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
    }.toString()

    private fun Regex.findAll(
        text: String,
        includedRanges: List<IntRange>
    ) = includedRanges.flatMap { part ->
        findAll(text.substring(part))
            .map { (it.range.first + part.first)..(it.range.last + part.first) }
    }

    private fun String.getExcludeRanges(includedRanges: List<IntRange>): List<IntRange> {
        val ranges = includedRanges.sortedBy(IntRange::first)
        return (sequenceOf(
            0 until (ranges.firstOrNull()?.first ?: length),
            (ranges.lastOrNull()?.last?.inc() ?: 0) until length
        ) + ranges.indices.mapNotNull { i ->
            when {
                i < ranges.size - 1 -> ranges[i].last + 1 until ranges[i + 1].first
                else -> null
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
            .sortedBy(IntRange::first)
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

    sealed interface MessageObject {
        class Text(val text: String) : MessageObject
        class Field(val field: Form) : MessageObject
        class Image(val file: UsedeskFile) : MessageObject
    }

    private fun <PARENT, OUT : PARENT, IN : PARENT> String.parts(
        regex: Regex,
        outConverter: String.() -> List<OUT>,
        inConverter: String.() -> List<IN>
    ): List<PARENT> {
        val ranges = regex.findAll(this)
            .map(MatchResult::range)
            .toList()
        val indexes = ranges.flatMap { sequenceOf(it.first, it.last + 1) } +
                sequenceOf(length)
        var i = 0
        return indexes.toSet().map { index ->
            val part = when (index) {
                i -> ""
                else -> substring(i, index)
            }
            when (i until index) {
                in ranges -> part.inConverter()
                else -> part.outConverter()
            }.apply { i = index }
        }.flatten()
    }

    private fun String.toMessageImage(): MessageObject {
        val section = drop(2).dropLast(1)
        val fileName = section.substringBefore("](")
        val fileUrl = section.substringAfter("](")

        val image = UsedeskFile.create(
            fileUrl,
            "image/*",
            "0",
            fileName
        )

        return MessageObject.Image(image)
    }

    private fun String.toMessageObjects() = parts(
        objectRegex,
        inConverter = { toMessageObject() },
        outConverter = {
            parts(
                imageRegexp,
                outConverter = { listOf(MessageObject.Text(this)) },
                inConverter = { listOf(toMessageImage()) }
            )
        }
    )

    private fun String.toMessageButton(): List<MessageObject>? {
        val parts = drop(9)
            .dropLast(2)
            .split(";")
        return when (parts.size) {
            4 -> {
                val buttonObject = MessageObject.Field(
                    Form.Button(
                        fieldId.decrementAndGet(),
                        parts[0],
                        parts[1],
                        parts[2]
                    )
                )
                when (parts[3]) {
                    "show" -> listOf(MessageObject.Text(buttonObject.field.name), buttonObject)
                    else -> listOf(buttonObject)
                }
            }
            else -> null
        }
    }

    private fun String.toMessageField(): List<MessageObject>? {
        val parts = drop(7)
            .dropLast(2)
            .split(";")
        return when (parts.size) {
            2, 3 -> valueOrNull {
                val associate = parts[1]
                val textType = when (associate) {
                    "email" -> Type.EMAIL
                    "phone" -> Type.PHONE
                    "name" -> Type.NAME
                    "note" -> Type.NOTE
                    "position" -> Type.POSITION
                    else -> null
                }
                val required = parts.getOrNull(2) == "true"
                listOf(
                    MessageObject.Field(
                        when (textType) {
                            null -> Form.Field.List(
                                associate.toLong(),
                                parts[0],
                                required,
                                listOf(),
                                null
                            )
                            else -> Form.Field.Text(
                                fieldId.decrementAndGet(),
                                parts[0],
                                required,
                                textType
                            )
                        }
                    )
                )
            }
            else -> null
        }
    }

    private fun String.toMessageObject() = when {
        buttonRegex.matches(this) -> toMessageButton()
        fieldRegex.matches(this) -> toMessageField()
        else -> null
    } ?: listOf(MessageObject.Text(this))

    companion object {
        private const val OBJECT_ANY = """[^\{\}]*"""
        private const val OBJECT_PART = """[^\{\};]*;"""
    }
}