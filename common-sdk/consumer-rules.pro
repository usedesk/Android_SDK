# Модели Gson: без keep R8 переименует поля и они перестанут совпадать с ключами JSON.
# Правила Gson накрывают только поля с @SerializedName, здесь их нет.
-keepclassmembers class ru.usedesk.common_sdk.api.entity.** { *; }
