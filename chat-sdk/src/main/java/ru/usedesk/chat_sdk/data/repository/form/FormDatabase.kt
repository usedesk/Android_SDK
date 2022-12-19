package ru.usedesk.chat_sdk.data.repository.form

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.usedesk.chat_sdk.data.repository.form.entity.DbForm

@Database(entities = [DbForm::class], version = 1)
internal abstract class FormDatabase : RoomDatabase() {
    abstract fun formDao(): FormDao
}