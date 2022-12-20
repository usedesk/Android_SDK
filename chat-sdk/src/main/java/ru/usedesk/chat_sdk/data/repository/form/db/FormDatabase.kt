package ru.usedesk.chat_sdk.data.repository.form.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DbForm::class], version = 1)
internal abstract class FormDatabase : RoomDatabase() {
    abstract fun formDao(): FormDao
}