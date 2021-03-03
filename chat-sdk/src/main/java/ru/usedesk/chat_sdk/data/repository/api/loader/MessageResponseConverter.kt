package ru.usedesk.chat_sdk.data.repository.api.loader

import ru.usedesk.chat_sdk.data.Converter
import ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.message.MessageResponse
import ru.usedesk.chat_sdk.entity.*
import ru.usedesk.common_sdk.utils.UsedeskDateUtil.Companion.getLocalCalendar
import toothpick.InjectConstructor

@InjectConstructor
internal class MessageResponseConverter : Converter<MessageResponse.Message?, List<UsedeskMessage>>() {

    override fun convert(from: MessageResponse.Message?): List<UsedeskMessage> {
        return convertOrNull {
            val fromClient = when (from!!.type) {
                MessageResponse.TYPE_CLIENT_TO_OPERATOR,
                MessageResponse.TYPE_CLIENT_TO_BOT -> {
                    true
                }
                MessageResponse.TYPE_OPERATOR_TO_CLIENT,
                MessageResponse.TYPE_BOT_TO_CLIENT -> {
                    false
                }
                else -> null
            }!!

            val createdAt = from.createdAt!!

            val messageDate = try {
                getLocalCalendar("yyyy-MM-dd'T'HH:mm:ss'Z'", createdAt)
            } catch (e: Exception) {
                getLocalCalendar("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", createdAt)
            }

            val id = from.payload?.messageId ?: from.id!!
            val name = from.name ?: ""
            val avatar = from.payload?.avatar ?: ""

            val fileMessage = convertOrNull {
                if (from.file != null) {
                    val file = UsedeskFile.create(from.file!!.content!!,
                            from.file!!.type,
                            from.file!!.size!!,
                            from.file!!.name!!)

                    if (file.isImage()) {
                        if (fromClient) {
                            UsedeskMessageClientImage(id, messageDate, file, UsedeskMessageClient.Status.RECEIVED)
                        } else {
                            UsedeskMessageAgentImage(id, messageDate, file, name, avatar)
                        }
                    } else {
                        if (fromClient) {
                            UsedeskMessageClientFile(id, messageDate, file, UsedeskMessageClient.Status.RECEIVED)
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

                    var convertedText = from.text!!
                            .replace("<strong data-verified=\"redactor\" data-redactor-tag=\"strong\">", "<b>")
                            .replace("</strong>", "</b>")
                            .replace("<em data-verified=\"redactor\" data-redactor-tag=\"em\">", "<i>")
                            .replace("</em>", "</i>")
                            .replace("</p>", "")
                            .removePrefix("<p>")
                            .trim('\u200B')
                            .trim()

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

                    if (convertedText.isEmpty() && buttons.isEmpty()) {
                        null
                    } else if (fromClient) {
                        UsedeskMessageClientText(id,
                                messageDate,
                                convertedText,
                                UsedeskMessageClient.Status.RECEIVED)
                    } else {
                        UsedeskMessageAgentText(id,
                                messageDate,
                                convertedText,
                                buttons,
                                feedbackNeeded,
                                feedback,
                                name,
                                avatar)
                    }
                } else {
                    null
                }
            }

            listOfNotNull(textMessage, fileMessage)
        } ?: listOf()
    }

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
        return if (sections.size == 4) {
            val text = sections[0]
            val url = sections[1]
            val type = sections[2]
            val isShow = sections[3] == "show"
            UsedeskMessageButton(text, url, type, isShow)
        } else {
            null
        }
    }
}