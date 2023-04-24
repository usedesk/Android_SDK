
package ru.usedesk.chat_sdk.data.repository.form.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface FormDao {
    @Query("SELECT * FROM DbForm WHERE id = :formId")
    fun get(formId: Long): DbForm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(dbForm: DbForm)
}