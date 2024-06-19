package ru.usedesk.chat_sdk.data.repository._extra

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import ru.usedesk.chat_sdk.data.repository.form.entity.DbForm
import ru.usedesk.chat_sdk.data.repository.form.entity.FormDao

@Database(
    entities = [DbForm::class],
    version = 2,
    autoMigrations = [AutoMigration(1, 2)]
)
internal abstract class ChatDatabase : RoomDatabase() {
    abstract fun formDao(): FormDao

    companion object {
        const val DATABASE_NAME = "usedesk_chat_sdk"
    }
}