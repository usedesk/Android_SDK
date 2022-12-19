package ru.usedesk.chat_sdk.data.repository.form

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import ru.usedesk.chat_sdk.data.repository.form.entity.DbForm

@Dao
internal interface FormDao {
    @RawQuery
    fun get(query: SupportSQLiteQuery): DbForm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(dbForm: DbForm)
}