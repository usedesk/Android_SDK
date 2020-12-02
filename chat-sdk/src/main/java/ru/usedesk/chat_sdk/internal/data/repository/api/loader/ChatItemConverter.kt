package ru.usedesk.chat_sdk.internal.data.repository.api.loader

import ru.usedesk.chat_sdk.external.entity.*
import ru.usedesk.chat_sdk.internal.data.Converter
import ru.usedesk.chat_sdk.internal.domain.entity.Message
import ru.usedesk.chat_sdk.internal.domain.entity.UsedeskFile
import toothpick.InjectConstructor
import java.text.SimpleDateFormat
import java.util.*

@InjectConstructor
internal class ChatItemConverter : Converter<Message?, List<UsedeskChatItem>>() {

    override fun convert(from: Message?): List<UsedeskChatItem> {
        return convertOrNull {
            val fromClient = when (from!!.type) {
                Message.TYPE_CLIENT_TO_OPERATOR,
                Message.TYPE_CLIENT_TO_BOT -> {
                    true
                }
                Message.TYPE_OPERATOR_TO_CLIENT,
                Message.TYPE_BOT_TO_CLIENT -> {
                    false
                }
                else -> null
            }!!

            val createdAt = from.createdAt!!

            val messageDate = Calendar.getInstance().apply {
                time = try {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK)
                            .parse(createdAt)!!
                } catch (e: Exception) {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
                            .parse(createdAt)!!
                }

                val hoursOffset = TimeZone.getDefault().rawOffset / 3600000
                add(Calendar.HOUR, hoursOffset)
            }

            val id = from.id!!
            val name = from.name ?: ""
            val avatar = from.payload?.avatar ?: ""

            val fileMessage = convertOrNull {
                if (from.file != null) {
                    val file = UsedeskFile(from.file!!.content!!,
                            from.file!!.type!!,
                            from.file!!.size!!,
                            from.file!!.name!!)

                    if (file.isImage()) {
                        if (fromClient) {
                            UsedeskMessageClientImage(id, messageDate, file)
                        } else {
                            UsedeskMessageAgentImage(id, messageDate, file, name, avatar)
                        }
                    } else {
                        if (fromClient) {
                            UsedeskMessageClientFile(id, messageDate, file)
                        } else {
                            UsedeskMessageAgentFile(id, messageDate, file, name, avatar)
                        }
                    }
                } else {
                    null
                }
            }

            val textMessage = convertOrNull {
                if (from.text?.isNotEmpty() == true) {
                    val text: String
                    val html: String

                    val divIndex = from.text!!.indexOf("<div")

                    if (divIndex >= 0) {
                        text = from.text!!.substring(0, divIndex)

                        html = from.text!!.removePrefix(text)
                    } else {
                        text = from.text!!
                        html = ""
                    }

                    val convertedText = text
                            .replace("<strong data-verified=\"redactor\" data-redactor-tag=\"strong\">", "<b>")
                            .replace("</strong>", "</b>")
                            .replace("<em data-verified=\"redactor\" data-redactor-tag=\"em\">", "<i>")
                            .replace("</em>", "</i>")
                            .replace("</p>", "")
                            .removePrefix("<p>")
                            .trim('\u200B')
                            .trim()

                    if (convertedText.isEmpty() && html.isEmpty()) {
                        null
                    } else if (fromClient) {
                        UsedeskMessageClientText(id, messageDate, convertedText, html)
                    } else {
                        UsedeskMessageAgentText(id, messageDate, convertedText, html, name, avatar)
                    }
                } else {
                    null
                }
            }

            listOfNotNull(textMessage, fileMessage)
        } ?: listOf()
    }
}