# Модели Gson: без keep R8 переименует поля и они перестанут совпадать с ключами JSON.
# UsedeskChatConfiguration и NotSentMessage сериализуются в SharedPreferences.
-keepclassmembers class ru.usedesk.chat_sdk.entity.UsedeskChatConfiguration { *; }
-keepclassmembers class ru.usedesk.chat_sdk.entity.UsedeskFile { *; }
-keepclassmembers class ru.usedesk.chat_sdk.data.repository.api.entity.** { *; }
-keepclassmembers class ru.usedesk.chat_sdk.data.repository.api.loader.socket._entity.** { *; }
-keepclassmembers class ru.usedesk.chat_sdk.data.repository.messages.MessagesRepository$NotSentMessage { *; }
-keepclassmembers class ru.usedesk.chat_sdk.data.repository.form.entity.** { *; }
